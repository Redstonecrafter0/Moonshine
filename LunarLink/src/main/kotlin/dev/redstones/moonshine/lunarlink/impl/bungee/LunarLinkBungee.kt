package dev.redstones.moonshine.lunarlink.impl.bungee

import dev.redstones.moonshine.common.token.RoutingToken
import dev.redstones.moonshine.lunarlink.LunarLink
import dev.redstones.moonshine.lunarlink.PluginEnvironment
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import dev.redstones.moonshine.lunarlink.proxy.BungeeProxy
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LunarLinkBungee: Plugin(), LunarLink {

    override val pluginEnvironment = PluginEnvironment.BUNGEE
    override val proxy by lazy { BungeeProxy(ProxyServer.getInstance()) }
    override val port: Int
        get() = getProxy().config.listeners.first().host.port

    override val connectionCount: Int
        get() = ProxyServer.getInstance().onlineCount

    override val loggerImpl: Logger = LoggerFactory.getLogger(logger.name)

    override fun onEnable() = enable()
    override fun onDisable() = disable()

    override fun enableHandshakeVerifier() {
    }

}

fun ProxiedPlayer.connectToServer(serverId: String) {
    val actualHost = "127.0.0.1"
    val handshakeField = pendingConnection::class.java.getDeclaredField("handshake")
    handshakeField.isAccessible = true
    val handshake = handshakeField.get(pendingConnection)
    val hostField = handshake::class.java.getDeclaredField("host")
    hostField.set(handshake, RoutingToken(serverId).sign(ByteArray(0)))
}
