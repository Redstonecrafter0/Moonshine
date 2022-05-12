package net.redstonecraft.redstonecloud.plugin

import com.google.inject.Inject
import net.redstonecraft.redstonecloud.plugin.proxy.DummyProxy
import org.slf4j.Logger
import org.spongepowered.api.Game
import org.spongepowered.api.Server
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.lifecycle.StartedEngineEvent
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent

class RedstonecloudSponge: RedstonecloudPlugin {

    override val pluginEnvironment = PluginEnvironment.SPONGE
    override val proxy = DummyProxy
    override val connectionCount: Int = 0
    @Inject private lateinit var logger: Logger
    override val port: Int
        get() = game.server().boundAddress().get().port

    @Inject
    private lateinit var game: Game

    override val loggerImpl: Logger
        get() = logger

    @Listener
    fun onServerStart(event: StartedEngineEvent<Server>) = enable()

    @Listener
    fun onServerStop(event: StoppingEngineEvent<Server>) = disable()
}
