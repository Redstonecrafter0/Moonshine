package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.protocol.Direction
import dev.redstones.moonshine.protocol.LegacyPacketReader
import dev.redstones.moonshine.protocol.packet.*
import dev.redstones.moonshine.protocol.slp.ServerListPing
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull

class LegacyClient(connection: Connection, selectorManager: SelectorManager): BaseClient(connection, selectorManager) {

    private fun readPackets() = LegacyPacketReader.read(Direction.Inbound, connection.input)

    override suspend fun handleClientConnection() {
        Gateway.logger.info("received legacy status request from {}", address)
        val serverListPing = (readPackets()
            .drop(1) // LegacyPacketInServerListPing. already checked for in protocol detection
            .firstOrNull() as? LegacyPacketInPluginMessage)
            ?.decodePingHostMessage() ?: return
        sendLegacyServerList(pingDownstream(serverListPing.host, serverListPing.port))
    }

    private suspend fun sendLegacyServerList(serverListPing: ServerListPing) {
        Gateway.logger.trace("sending legacy status response to {}", address)
        val serverVersion = serverListPing.version.name
        val motd = serverListPing.legacyDescription
        val playerCount = serverListPing.players?.online ?: 0
        val maxPlayerCount = serverListPing.players?.max ?: -1
        LegacyPacketOutKick(127, serverVersion, motd, playerCount, maxPlayerCount).write(connection.output) // 127 is an invalid protocol version
        connection.output.flush()
    }

}
