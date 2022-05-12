package net.redstonecraft.redstonecloud.plugin

import net.redstonecraft.redstonecloud.plugin.proxy.DummyProxy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RedstonecloudBukkit: JavaPlugin(), RedstonecloudPlugin {

    override val pluginEnvironment = PluginEnvironment.BUKKIT
    override val proxy = DummyProxy
    override val port: Int
        get() = server.port

    override val loggerImpl: Logger = LoggerFactory.getLogger(logger.name)

    override val connectionCount: Int
        get() = Bukkit.getServer().onlinePlayers.size

    override fun onEnable() = enable()
    override fun onDisable() = disable()

}
