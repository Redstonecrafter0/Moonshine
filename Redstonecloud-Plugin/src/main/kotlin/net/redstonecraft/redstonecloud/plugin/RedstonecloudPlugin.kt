package net.redstonecraft.redstonecloud.plugin

import net.redstonecraft.redstonecloud.plugin.proxy.IProxy
import org.slf4j.Logger
import net.redstonecraft.redstonecloud.plugin.metrics.startMetricsServer

/**
 * The entrypoint where all the magic happens.
 *
 * @property pluginEnvironment Specifies what system is running this plugin.
 * @property proxy Reference to the ProxyServer to modify known Servers.
 * @property connectionCount Gets the current amount of open connections.
 * @property loggerImpl Reference to the internal Logger over SLF4J.
 * @property port Returns the port the server is running on.
 * */
sealed interface RedstonecloudPlugin {

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
                private val dbPort = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_PORT") ?: error("No db port")
                private val dbName = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_NAME") ?: error("No database")
                private val dbUser = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_USERNAME") ?: error("No db user")
                private val dbPass = System.getenv("REDSTONECLOUD_SUB_SERVER_DB_PASSWORD") ?: error("No db password")
            }
            object Proxy {
                private val dbHost = System.getenv("REDSTONECLOUD_PROXY_DB_HOST") ?: error("No db host")
                private val dbPort = System.getenv("REDSTONECLOUD_PROXY_DB_PORT") ?: error("No db port")
                private val dbName = System.getenv("REDSTONECLOUD_PROXY_DB_NAME") ?: error("No database")
                private val dbUser = System.getenv("REDSTONECLOUD_PROXY_DB_USERNAME") ?: error("No db user")
                private val dbPass = System.getenv("REDSTONECLOUD_PROXY_DB_PASSWORD") ?: error("No db password")
            }
        }
        private val isSubServer = System.getenv("REDSTONECLOUD_SUB_SERVER")?.toBoolean() ?: false
        private val isProxy = System.getenv("REDSTONECLOUD_PROXY")?.toBoolean() ?: false
        lateinit var realInstance: RedstonecloudPlugin
            private set
        val pluginEnvironment: PluginEnvironment by realInstance::pluginEnvironment
        val proxy: IProxy by realInstance::proxy
        val connectionCount: Int by realInstance::connectionCount
        val logger: Logger by realInstance::loggerImpl
    }

    fun enable() {
        realInstance = this
        if (isProxy && pluginEnvironment.isProxy) {
        }
        startMetricsServer()
    }

    fun disable() {
    }

}
