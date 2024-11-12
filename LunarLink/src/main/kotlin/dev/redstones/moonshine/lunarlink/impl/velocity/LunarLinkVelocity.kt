package dev.redstones.moonshine.lunarlink.impl.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import dev.redstones.moonshine.lunarlink.LunarLink
import dev.redstones.moonshine.lunarlink.PluginEnvironment
import dev.redstones.moonshine.lunarlink.proxy.VelocityProxy
import org.slf4j.Logger

@Plugin(
    id = "lunarlink",
    name = "LunarLink",
    version = "0.1.0",
    description = "Velocity integration for a cloud-native Minecraft network.",
    url = "https://github.com/Redstonecrafter0/Moonshine",
    authors = ["Redstonecrafter0"]
)
class LunarLinkVelocity @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger
): LunarLink {

    override val pluginEnvironment = PluginEnvironment.VELOCITY
    override val proxy = VelocityProxy(server)

    override val loggerImpl: Logger
        get() = logger
    override val connectionCount: Int
        get() = server.playerCount
    override val port: Int
        get() = server.boundAddress.port

    @Subscribe(order = PostOrder.FIRST)
    fun onInit(event: ProxyInitializeEvent) = enable()

    @Subscribe(order = PostOrder.LAST)
    fun onDisable(event: ProxyShutdownEvent) = disable()

}
