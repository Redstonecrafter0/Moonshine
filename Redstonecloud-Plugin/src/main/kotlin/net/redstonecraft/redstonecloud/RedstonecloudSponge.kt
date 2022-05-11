package net.redstonecraft.redstonecloud

import com.google.inject.Inject
import net.redstonecraft.redstonecloud.proxy.DummyProxy
import org.slf4j.Logger
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.event.game.state.GameStoppingServerEvent
import org.spongepowered.api.plugin.Plugin

@Plugin(
    id = "redstonecloud-plugin",
    name = "Redstonecloud-Plugin",
    version = "1.0.0",
    description = "Velocity integration for a cloud-native Minecraft network",
    url = "https://github.com/Redstonecrafter0/Redstonecloud",
    authors = ["Redstonecrafter0"]
)
class RedstonecloudSponge: RedstonecloudPlugin {

    override val pluginEnvironment = PluginEnvironment.SPONGE
    override val proxy = DummyProxy
    override val connectionCount: Int = 0
    @Inject private lateinit var logger: Logger

    override val loggerImpl: Logger
        get() = logger

    @Listener
    fun onServerStart(event: GameStartedServerEvent) = enable()

    @Listener
    fun onServerStop(event: GameStoppingServerEvent) = disable()
}
