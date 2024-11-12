package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.common.protocol.*
import dev.redstones.moonshine.common.protocol.packet.*
import dev.redstones.moonshine.common.token.RoutingToken
import dev.redstones.moonshine.common.protocol.channel.LimitedSizeByteReadChannel
import dev.redstones.moonshine.common.protocol.dns.IServerIdResolver
import dev.redstones.moonshine.common.protocol.slp.ServerListPing
import dev.redstones.moonshine.common.protocol.slp.ServerListPinger
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
        return PacketReader.read(Direction.Inbound, state, connection.input) as? PacketInHandshakingHandshake ?: throw ProtocolException("First packet must be Handshake")
    }

    suspend fun readPacket(): IPacket {
        return PacketReader.read(Direction.Inbound, state, connection.input)
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

    suspend fun handleStatusRequest() {
        IngressController.logger.debug("received status request from {}", address)
        val response = ServerListPinger.ping("127.0.0.1", 25577) ?: ServerListPing(
            ServerListPing.Version("failed", 127),
            ServerListPing.Players(-1, 0, emptyList()),
        )
        IngressController.logger.trace("sending status response to {}", address)
        PacketOutStatusStatusResponse(Json.encodeToString(response)).write(connection.output)
    }

    val expectedLegacyPingHead = byteArrayOf(0x01, 0xFA.toByte(), 0x00, 0x0B, 0x00, 0x4D, 0x00, 0x43, 0x00, 0x7C, 0x00, 0x50, 0x00, 0x69, 0x00, 0x6E, 0x00, 0x67, 0x00, 0x48, 0x00, 0x6F, 0x00, 0x73, 0x00, 0x74)

    suspend fun handleLegacyServerList() {
        IngressController.logger.info("received legacy status request from {}", address)
        // 0XFE already read
        for (i in expectedLegacyPingHead) {
            if (connection.input.readByte() != i) {
                return
            }
        }
        val remaining = connection.input.readShort()
        val channel = LimitedSizeByteReadChannel(connection.input, remaining.toInt())
        channel.readByte() // protocol version
        val host = channel.legacyReadString(128)
        val port = channel.readInt()
        if (channel.pos != channel.size) {
            throw ProtocolException("malformed legacy packet")
        }
        sendLegacyServerList()
    }

    private suspend fun sendLegacyServerList() {
        val response = ServerListPinger.ping("127.0.0.1", 25577) ?: ServerListPing(
            ServerListPing.Version("failed", 127),
            ServerListPing.Players(-1, 0, emptyList()),
            "{\"text\": \"failed\"}",
            ""
        )
        IngressController.logger.trace("sending legacy status response to {}", address)
        connection.output.writeByte(0xFF.toByte()) // packetId
        val serverVersion = response.version.name
        val motd = response.legacyDescription
        val playerCount = response.players?.online?.toString() ?: "0"
        val maxPlayerCount = response.players?.max?.toString() ?: "-1"
        connection.output.legacyWriteString("§1\u0000127\u0000$serverVersion\u0000$motd\u0000$playerCount\u0000$maxPlayerCount") // 127 is an invalid protocol version
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
