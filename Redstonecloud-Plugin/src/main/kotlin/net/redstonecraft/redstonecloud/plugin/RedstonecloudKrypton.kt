package net.redstonecraft.redstonecloud.plugin

import com.google.inject.Inject
import net.redstonecraft.redstonecloud.plugin.proxy.DummyProxy
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.spi.ExtendedLogger
import org.apache.logging.slf4j.Log4jLogger
import org.apache.logging.slf4j.Log4jMarkerFactory
import org.kryptonmc.api.Server
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.ServerStartEvent
import org.kryptonmc.api.event.server.ServerStopEvent
import org.kryptonmc.api.plugin.annotation.Plugin

@Plugin(
    id = "redstonecloud-plugin",
    name = "Redstonecloud-Plugin",
    version = "0.1.0",
    description = "Krypton integration for a cloud-native Minecraft network.",
    authors = ["Redstonecrafter0"]
)
class RedstonecloudKrypton @Inject constructor(val server: Server, val logger: Logger): RedstonecloudPlugin {

    override val pluginEnvironment = PluginEnvironment.KRYPTON
    override val proxy = DummyProxy
    override val connectionCount: Int
        get() = server.players.size
    override val loggerImpl = Log4jLogger(Log4jMarkerFactory(), logger as ExtendedLogger, logger.name)
    override val port: Int
        get() = server.address.port

    @Listener
    fun onStart(event: ServerStartEvent) = enable()

    @Listener
    fun onStop(event: ServerStopEvent) = disable()

}
