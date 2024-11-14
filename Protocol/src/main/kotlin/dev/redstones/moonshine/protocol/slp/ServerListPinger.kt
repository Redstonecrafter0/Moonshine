package dev.redstones.moonshine.protocol.slp

import dev.redstones.moonshine.protocol.Direction
import dev.redstones.moonshine.protocol.ProtocolPacketReader
import dev.redstones.moonshine.protocol.State
import dev.redstones.moonshine.protocol.packet.PacketInHandshakingHandshake
import dev.redstones.moonshine.protocol.packet.PacketInStatusStatusRequest
import dev.redstones.moonshine.protocol.packet.PacketOutStatusStatusResponse
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object ServerListPinger {

    private val selectorManager = SelectorManager(Dispatchers.IO)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun ping(host: String, port: Int): ServerListPing? {
        return withContext(Dispatchers.IO) {
            val socket = aSocket(selectorManager).tcp().connect(host, port) {
                socketTimeout = 30000
            }
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(false)
            PacketInHandshakingHandshake(-1, host, port, State.Status).write(sendChannel)
            PacketInStatusStatusRequest.write(sendChannel)
            val packet = ProtocolPacketReader.read(Direction.Outbound, State.Status, receiveChannel) as? PacketOutStatusStatusResponse
                ?: return@withContext null
            socket.close()
            json.decodeFromString<ServerListPing>(packet.jsonResponse)
        }
    }

}
