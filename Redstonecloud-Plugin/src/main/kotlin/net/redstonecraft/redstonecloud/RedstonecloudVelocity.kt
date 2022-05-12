package net.redstonecraft.redstonecloud

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.redstonecraft.redstonecloud.proxy.VelocityProxy
import org.slf4j.Logger

@Plugin(
    id = "redstonecloud-plugin",
    name = "Redstonecloud-Plugin",
    version = "1.0.0",
    description = "Velocity integration for a cloud-native Minecraft network.",
    url = "https://github.com/Redstonecrafter0/Redstonecloud",
    authors = ["Redstonecrafter0"]
)
class RedstonecloudVelocity @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger
): RedstonecloudPlugin {

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
