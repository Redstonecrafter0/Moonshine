package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.common.dns.IServerIdResolver
import dev.redstones.moonshine.common.token.RoutingToken
import dev.redstones.moonshine.packet.ProtocolException
import dev.redstones.moonshine.protocol.Direction
import dev.redstones.moonshine.protocol.ProtocolPacketReader
import dev.redstones.moonshine.protocol.State
import dev.redstones.moonshine.protocol.packet.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class MinecraftClient(connection: Connection, selectorManager: SelectorManager): BaseClient(connection, selectorManager) {

    private var state = State.Handshaking

    private var downstream: Connection? = null

    private fun readPackets(stateChannel: ReceiveChannel<State>) = ProtocolPacketReader.read(Direction.Inbound, stateChannel, connection.input)

    override suspend fun handleClientConnection() {
        lateinit var handshakePacket: PacketInHandshakingHandshake
        val stateChannel = Channel<State>(1)
        stateChannel.send(State.Handshaking)
        readPackets(stateChannel).collect { packet ->
            when (packet) {
                is PacketInHandshakingHandshake -> {
                    state = packet.nextState
                    handshakePacket = packet
                    Gateway.logger.trace("received {}", packet)
                }
                is PacketInStatusStatusRequest -> handleStatusRequest(handshakePacket)
                is PacketInStatusPingRequest -> handlePing(packet)
                is PacketInLoginStartLogin -> login(handshakePacket, packet.name, packet.uuid, selectorManager, RoutingToken.Verifier.randomVerifier(), "eu-central-1-lobby", object: IServerIdResolver {
                    override fun resolveRoutingToken(token: RoutingToken): Pair<String, Int> {
                        return "localhost" to 25577
                    }
                })
                else -> throw ProtocolException("unexpected packet $packet in state $state")
            }
            stateChannel.send(state)
        }
        if (!isClosed && state == State.Proxied) {
            forwardData()
        }
    }

    private suspend fun login(
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
        Gateway.logger.trace("Starting downstream connection from {} to {}", address, "${downstreamHandshake.host}:${downstreamHandshake.port}")
        downstream = aSocket(selectorManager).tcp().connect(downstreamHandshake.host, downstreamHandshake.port) {
            socketTimeout = 30000
        }.connection()
        downstreamHandshake.write(downstream!!.output)
        PacketInLoginStartLogin(name, uuid).write(downstream!!.output)
        Gateway.logger.info("Downstream logged into from {}", address)
        state = State.Proxied
    }

    private suspend fun forwardData() {
        withContext(Dispatchers.IO) {
            Gateway.logger.debug("Starting downstream proxying for {}", address)
            val sendUp = launch {
                downstream!!.input.copyTo(connection.output)
            }
            val sendDown = launch {
                connection.input.copyTo(downstream!!.output)
            }
            Gateway.logger.trace("Started downstream proxying for {}", address)
            joinAll(sendUp, sendDown)
            Gateway.logger.trace("Finished downstream proxying for {}", address)
            throw ProtocolException("connection closed")
        }
    }

    suspend fun handleStatusRequest(handshakePacket: PacketInHandshakingHandshake) {
        Gateway.logger.info("received status request from {}", address)
        val response = pingDownstream(handshakePacket.host, handshakePacket.port)
        Gateway.logger.trace("sending status response to {}", address)
        PacketOutStatusStatusResponse(Json.encodeToString(response)).write(connection.output)
    }

    suspend fun handlePing(packet: PacketInStatusPingRequest) {
        Gateway.logger.trace("received ping from {}", address)
        Gateway.logger.trace("sending pong to {}", address)
        PacketOutStatusPingResponse(packet.payload).write(connection.output)
        state = State.Close
    }

    override fun close() {
        super.close()
        downstream?.socket?.close()
    }

}
