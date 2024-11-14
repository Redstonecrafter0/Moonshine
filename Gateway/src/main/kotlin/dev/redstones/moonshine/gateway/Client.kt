package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.protocol.*
import dev.redstones.moonshine.protocol.packet.*
import dev.redstones.moonshine.common.token.RoutingToken
import dev.redstones.moonshine.common.dns.IServerIdResolver
import dev.redstones.moonshine.packet.IPacket
import dev.redstones.moonshine.packet.ProtocolException
import dev.redstones.moonshine.protocol.slp.ServerListPing
import dev.redstones.moonshine.protocol.slp.ServerListPinger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.util.UUID

class Client(private val connection: Connection): Closeable {

    val address: String
        get() = "${(connection.socket.remoteAddress as InetSocketAddress).hostname}:${(connection.socket.remoteAddress as InetSocketAddress).port}"

    var state = State.Handshaking

    private var downstream: Connection? = null

    suspend fun readHandshakePacket(): PacketInHandshakingHandshake {
        return ProtocolPacketReader.read(Direction.Inbound, state, connection.input) as? PacketInHandshakingHandshake ?: throw ProtocolException("First packet must be Handshake")
    }

    suspend fun readPacket(): IPacket<Int> {
        return ProtocolPacketReader.read(Direction.Inbound, state, connection.input)
    }

    suspend fun login(
        handshake: PacketInHandshakingHandshake,
        name: String,
        uuid: UUID,
        selectorManager: SelectorManager,
        verifier: RoutingToken.Verifier,
        fallbackId: String,
        resolver: IServerIdResolver
    ) {
        val serverId = verifier.verify(handshake.host) ?: RoutingToken(fallbackId)
        val (host, port) = resolver.resolveRoutingToken(serverId)
        val downstreamHandshake = PacketInHandshakingHandshake(handshake.protocolVersion, host, port, handshake.nextState)
        IngressController.logger.trace("Starting downstream connection from {} to {}", address, "${downstreamHandshake.host}:${downstreamHandshake.port}")
        downstream = aSocket(selectorManager).tcp().connect(downstreamHandshake.host, downstreamHandshake.port) {
            socketTimeout = 30000
        }.connection()
        downstreamHandshake.write(downstream!!.output)
        PacketInLoginStartLogin(name, uuid).write(downstream!!.output)
        IngressController.logger.info("Downstream logged into from {}", address)
        state = State.Proxied
    }

    suspend fun forwardData() {
        withContext(Dispatchers.IO) {
            IngressController.logger.debug("Starting downstream proxying for {}", address)
            val sendUp = launch {
                downstream!!.input.copyTo(connection.output)
            }
            val sendDown = launch {
                connection.input.copyTo(downstream!!.output)
            }
            IngressController.logger.trace("Started downstream proxying for {}", address)
            joinAll(sendUp, sendDown)
            IngressController.logger.trace("Finished downstream proxying for {}", address)
            throw ProtocolException("connection closed")
        }
    }

    private suspend fun pingDownstream(host: String, port: Int): ServerListPing {
        return ServerListPinger.ping("127.0.0.1", 25577) ?: ServerListPing(
            ServerListPing.Version("failed", 127),
            ServerListPing.Players(-1, 0, emptyList()),
        )
    }

    suspend fun handleStatusRequest(handshakePacket: PacketInHandshakingHandshake) {
        IngressController.logger.info("received status request from {}", address)
        val response = pingDownstream(handshakePacket.host, handshakePacket.port)
        IngressController.logger.trace("sending status response to {}", address)
        PacketOutStatusStatusResponse(Json.encodeToString(response)).write(connection.output)
    }

    suspend fun handleLegacyServerList() {
        IngressController.logger.info("received legacy status request from {}", address)
        if (LegacyPacketReader.read(Direction.Inbound, connection.input) !is LegacyPacketInServerListPing) return
        val serverListPing = (LegacyPacketReader.read(Direction.Inbound, connection.input) as? LegacyPacketInPluginMessage)?.decodePingHostMessage() ?: return
        sendLegacyServerList(pingDownstream(serverListPing.host, serverListPing.port))
    }

    private suspend fun sendLegacyServerList(serverListPing: ServerListPing) {
        IngressController.logger.trace("sending legacy status response to {}", address)
        val serverVersion = serverListPing.version.name
        val motd = serverListPing.legacyDescription
        val playerCount = serverListPing.players?.online ?: 0
        val maxPlayerCount = serverListPing.players?.max ?: -1
        LegacyPacketOutKick(127, serverVersion, motd, playerCount, maxPlayerCount).write(connection.output) // 127 is an invalid protocol version
        connection.output.flush()
    }

    suspend fun handlePing(packet: PacketInStatusPingRequest) {
        IngressController.logger.trace("received ping from {}", address)
        IngressController.logger.trace("sending pong to {}", address)
        PacketOutStatusPingResponse(packet.payload).write(connection.output)
    }

    override fun close() {
        connection.socket.close()
        downstream?.socket?.close()
    }

}
