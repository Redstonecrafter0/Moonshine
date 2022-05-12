package net.redstonecraft.redstonecloud.plugin

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.redstonecraft.redstonecloud.plugin.proxy.BungeeProxy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RedstonecloudBungee: Plugin(), RedstonecloudPlugin {

    override val pluginEnvironment = PluginEnvironment.BUNGEE
    override val proxy by lazy { BungeeProxy(ProxyServer.getInstance()) }
    override val port: Int
        get() = getProxy().config.listeners.first().host.port

    override val connectionCount: Int
        get() = ProxyServer.getInstance().onlineCount

    override val loggerImpl: Logger = LoggerFactory.getLogger(logger.name)

    override fun onEnable() = enable()
    override fun onDisable() = disable()

}
