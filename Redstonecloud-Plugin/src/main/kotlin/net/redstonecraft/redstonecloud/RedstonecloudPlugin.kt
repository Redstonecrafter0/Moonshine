package net.redstonecraft.redstonecloud

import net.redstonecraft.redstonecloud.discovery.UDPClient
import net.redstonecraft.redstonecloud.discovery.UDPServer
import net.redstonecraft.redstonecloud.proxy.IProxy
import org.slf4j.Logger
import java.net.InetAddress

sealed interface RedstonecloudPlugin {

    val pluginEnvironment: PluginEnvironment
    val proxy: IProxy
    val connectionCount: Int
    val loggerImpl: Logger

    companion object Instance {
        private val port = System.getenv("REDSTONECLOUD_DISCOVER_PORT")?.toIntOrNull() ?: 3515
        private val discoverHost: String? = System.getenv("REDSTONECLOUD_DISCOVER_ADDRESS") ?: "224.0.2.30"
        private val knownProxies = mutableSetOf<String>()
        private val subServer = mutableSetOf<String>()
        lateinit var realInstance: RedstonecloudPlugin private set
        val pluginEnvironment: PluginEnvironment get() = realInstance.pluginEnvironment
        val proxy: IProxy get() = realInstance.proxy
        val connectionCount: Int get() = realInstance.connectionCount
        val logger: Logger get() = realInstance.loggerImpl
    }

    fun enable() {
        realInstance = this
        if (pluginEnvironment.isProxy) {
            UDPServer(port) { it, address ->
                val (cmd, a) = it.drop(1).split(" ")
                when (cmd) {
                    "reg" -> knownProxies += a
                    "ureg" -> knownProxies -= a
                    "up" -> subServer += a
                    "down" -> subServer -= a
                }
                if (it[0] == '1') {
                    knownProxies.forEach { UDPClient(InetAddress.getByName(it), port).send("0$cmd $a\u0000") }
                    if (cmd == "reg") {
                        UDPClient(address, port).send("${knownProxies.joinToString(" ")}\n${subServer.joinToString(" ")}")
                    }
                }
            }.start()
            UDPClient(InetAddress.getByName(subDiscoverHost), port).send("1reg ${InetAddress.getLocalHost().hostAddress}\u0000") {
                val (proxies, subs) = it.split("\n")
                knownProxies += proxies.split(" ")
                subServer += subs.split(" ")
            }
        }
        if (discoverHost != null || !pluginEnvironment.isProxy) {
            UDPClient(InetAddress.getByName(discoverHost), port).send("1up ${InetAddress.getLocalHost().hostAddress}\u0000")
        }
    }

    fun disable() {
        if (discoverHost != null) {
            UDPClient(InetAddress.getByName(discoverHost), port).send("1down ${InetAddress.getLocalHost().hostAddress}\u0000")
        }
        if (pluginEnvironment.isProxy) {
            UDPClient(InetAddress.getByName(subDiscoverHost), port).send("1ureg ${InetAddress.getLocalHost().hostAddress}\u0000")
        }
    }

}
