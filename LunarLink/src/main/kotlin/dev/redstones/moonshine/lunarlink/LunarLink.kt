package dev.redstones.moonshine.lunarlink

import dev.redstones.moonshine.lunarlink.proxy.IProxy
import org.slf4j.Logger
import dev.redstones.moonshine.lunarlink.metrics.startMetricsServer
import dev.redstones.moonshine.lunarlink.proxy.ServerInfo
import redis.clients.jedis.Jedis
import java.net.InetSocketAddress
import java.util.*

/**
 * The entrypoint where all the magic happens.
 *
 * @property pluginEnvironment Specifies what system is running this plugin.
 * @property proxy Reference to the ProxyServer to modify known Servers.
 * @property connectionCount Gets the current amount of open connections.
 * @property loggerImpl Reference to the internal Logger over SLF4J.
 * @property port Returns the port the server is running on.
 * */
interface LunarLink {

    val pluginEnvironment: PluginEnvironment
    val proxy: IProxy
    val connectionCount: Int
    val loggerImpl: Logger
    val port: Int

    /**
     * Accessor for plugin properties and reference to the real plugin instance.
     *
     * @property pluginEnvironment Specifies what system is running this plugin.
     * @property proxy Reference to the ProxyServer to modify known Servers.
     * @property connectionCount Gets the current amount of open connections.
     * @property logger Reference to the internal Logger over SLF4J.
     * */
    companion object Instance {

        object Database {
            object SubServer {
                private val dbHost = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_HOST") ?: error("No db host")
                private val dbPort = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_PORT")?.toIntOrNull() ?: error("No db port")
                private val dbPass = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_PASSWORD")
                val dbSetKey = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_KEY_SET") ?: error("No db set key")
                val dbMapKey = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_KEY_MAP") ?: error("No db map key")
                val name = System.getenv("REDSTONECLOUD_SUB_SERVER_NAME") ?: error("No name")
                val podIp = System.getenv("REDSTONECLOUD_SUB_SERVER_IP") ?: error("No ip")
                val connection by lazy { Jedis(dbHost, dbPort).apply { if (dbPass != null) auth(dbPass) } }
            }
            object Proxy {
                private val dbHost = System.getenv("REDSTONECLOUD_PROXY_DB_HOST") ?: error("No db host")
                private val dbPort = System.getenv("REDSTONECLOUD_PROXY_DB_PORT")?.toIntOrNull() ?: error("No db port")
                private val dbPass = System.getenv("REDSTONECLOUD_PROXY_DB_PASSWORD")
                val dbSetKey = System.getenv("REDSTONECLOUD_PROXY_DB_KEY_SET") ?: error("No db set key")
                val dbMapKey = System.getenv("REDSTONECLOUD_PROXY_DB_KEY_MAP") ?: error("No db map key")
                val reload = System.getenv("REDSTONECLOUD_PROXY_REFRESH_TIME")?.toLongOrNull() ?: error("No refresh time")
                val connection by lazy { Jedis(dbHost, dbPort).apply { if (dbPass != null) auth(dbPass) } }
                val timer = Timer()
            }
        }
        private val isSubServer = System.getenv("REDSTONECLOUD_SUB_SERVER")?.toBoolean() ?: false
        private val isProxy = System.getenv("REDSTONECLOUD_PROXY")?.toBoolean() ?: false
        lateinit var realInstance: LunarLink
            private set
        val pluginEnvironment: PluginEnvironment by realInstance::pluginEnvironment
        val proxy: IProxy by realInstance::proxy
        val connectionCount: Int by realInstance::connectionCount
        val logger: Logger by realInstance::loggerImpl
    }

    fun enable() {
        realInstance = this
        startMetricsServer()
        if (isProxy && pluginEnvironment.isProxy) {
            Database.Proxy.timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val allServers = Database.Proxy.connection.smembers(Database.Proxy.dbSetKey)
                    val previousServers = proxy.server.map { it.name }.toSet()
                    val newServers = allServers - previousServers
                    val oldServers = previousServers - allServers
                    proxy.server.filter { it.name in oldServers }.forEach { proxy.removeServer(it) }
                    newServers.forEach {
                        proxy.addServer(ServerInfo(it, Database.Proxy.connection.hget(Database.Proxy.dbMapKey, it).toInetSocketAddress()))
                    }
                }
            }, 0L, Database.Proxy.reload)
        }
        if (isSubServer) {
            Database.SubServer.connection.hset(Database.SubServer.dbMapKey, Database.SubServer.name, "${Database.SubServer.podIp}:$port")
            Database.SubServer.connection.sadd(Database.SubServer.dbSetKey, Database.SubServer.name)
        }
    }

    fun enableHandshakeVerifier()

    fun disable() {
        if (isSubServer) {
            Database.SubServer.connection.srem(Database.SubServer.dbSetKey, Database.SubServer.name)
            Database.SubServer.connection.hdel(Database.SubServer.dbMapKey, Database.SubServer.name)
        }
    }

    private fun String.toInetSocketAddress(): InetSocketAddress {
        val (host, port) = split(":", limit = 2)
        return InetSocketAddress(host, port.toInt())
    }

}
