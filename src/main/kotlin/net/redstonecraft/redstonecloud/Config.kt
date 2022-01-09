package net.redstonecraft.redstonecloud

import kotlinx.serialization.Serializable
import java.io.File
import java.util.*

class ServerInfo(val config: ServerConfig) {

    val name: String
        get() = config.name

    val instances = mutableListOf<ServerInstance>()

    fun createInstance(): ServerInstance {
        val instance = ServerInstance(this)
        instances += instance
        return instance
    }

}

class ServerInstance(val serverInfo: ServerInfo, val id: UUID = UUID.randomUUID()) {

    val workingDirectory: File
        get() = File("", id.toString())

}

@Serializable
data class ServerConfig(
    val name: String,
    var serverHandling: ServerHandling,
    var maxInstances: Int,
    var maxPlayers: Int,
    var jre: Int,
    var jvmArgs: List<String>,
    var jar: String,
    var args: List<String>
)

enum class ServerHandling {
    STATIC, DYNAMIC, AUTO, SCRIPT
}
