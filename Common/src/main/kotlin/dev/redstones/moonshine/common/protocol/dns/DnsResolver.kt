package dev.redstones.moonshine.common.protocol.dns

import org.apache.commons.validator.routines.InetAddressValidator
import java.util.*
import javax.naming.Context
import javax.naming.NameNotFoundException
import javax.naming.directory.InitialDirContext

object DnsResolver {

    private val dirContext: InitialDirContext

    init {
        Class.forName("com.sun.jndi.dns.DnsContextFactory")
        val env = Hashtable<String, String>()
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.dns.DnsContextFactory"
        dirContext = InitialDirContext(env)
    }

    fun resolve(address: String, port: Int = 25565): Set<RecordSrv> {
        return try {
            val records = resolveSRV(address)
            records.ifEmpty {
                resolveName(address).map { RecordSrv(-1, -1, port, it, address) }.toSet()
            }
        } catch (_: NameNotFoundException) {
            // catch non existent names
            emptySet()
        }
    }

    fun resolveSRV(address: String): Set<RecordSrv> {
        val attribute = dirContext.getAttributes("_minecraft._tcp.$address", arrayOf("SRV"))["srv"] ?: return emptySet()
        return (0 until attribute.size()).asSequence()
            .map {
                val (priority, weight, port, host) = attribute.get(it).toString().split(" ", limit = 4)
                RecordSrv(priority.toInt(), weight.toInt(), port.toInt(), host.removeSuffix("."), address)
            }.map {
                when {
                    InetAddressValidator.getInstance().isValidInet4Address(it.address) -> listOf(it.address)
                    InetAddressValidator.getInstance().isValidInet6Address(it.address) -> listOf(it.address)
                    else -> resolveName(it.address)
                }.map { host -> RecordSrv(it.priority, it.weight, it.port, host, it.host) }
            }.flatten().toSet()
    }

    fun resolveName(address: String, depth: Int = 10): Set<String> {
        if (depth <= 0) return emptySet()
        return resolveA(address) + resolveAAAA(address) + resolveCNAME(address).map { host -> resolveName(host, depth - 1) }.flatten().toSet()
    }

    fun resolveA(address: String): Set<String> {
        val attribute = dirContext.getAttributes(address, arrayOf("A"))["a"] ?: return emptySet()
        return (0 until attribute.size()).map {
            attribute.get(it).toString()
        }.toSet()
    }

    fun resolveAAAA(address: String): Set<String> {
        val attribute = dirContext.getAttributes(address, arrayOf("AAAA"))["aaaa"] ?: return emptySet()
        return (0 until attribute.size()).map {
            attribute.get(it).toString()
        }.toSet()
    }

    fun resolveCNAME(address: String): Set<String> {
        val attribute = dirContext.getAttributes(address, arrayOf("CNAME"))["cname"] ?: return emptySet()
        return (0 until attribute.size()).map {
            attribute.get(it).toString().removeSuffix(".")
        }.toSet()
    }

}
