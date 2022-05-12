package net.redstonecraft.redstonecloud

import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import net.redstonecraft.redstonecloud.proxy.DummyProxy
import org.slf4j.Logger

class RedstonecloudMinestom: Extension(), RedstonecloudPlugin {

    override val pluginEnvironment = PluginEnvironment.MINESTOM
    override val proxy = DummyProxy
    override val connectionCount: Int
        get() = MinecraftServer.getConnectionManager().onlinePlayers.size
    override val loggerImpl: Logger
        get() = logger
    override val port: Int
        get() = MinecraftServer.getServer().port

    override fun initialize() = enable()
    override fun terminate() = disable()
}
