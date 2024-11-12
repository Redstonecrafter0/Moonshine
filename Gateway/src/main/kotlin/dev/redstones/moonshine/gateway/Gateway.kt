package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.common.token.RoutingToken
import dev.redstones.moonshine.common.protocol.ProtocolException
import dev.redstones.moonshine.common.protocol.packet.PacketInLoginStartLogin
import dev.redstones.moonshine.common.protocol.packet.PacketInStatusPingRequest
import dev.redstones.moonshine.common.protocol.packet.PacketInStatusStatusRequest
import dev.redstones.moonshine.common.protocol.State
import dev.redstones.moonshine.common.protocol.dns.IServerIdResolver
import dev.redstones.moonshine.common.util.addressString
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException

class IngressController {

    companion object {
        val logger = LoggerFactory.getLogger(IngressController::class.java)
    }

    private val selectorManager = SelectorManager(Dispatchers.IO)
    private val serverSocket = runBlocking {
        aSocket(selectorManager).tcp().bind("0.0.0.0", 25565) {
            typeOfService = TypeOfService.IPTOS_LOWDELAY
        }
    }

    init {
        val address = serverSocket.localAddress as InetSocketAddress
        logger.info("Listening on {}", "${address.hostname}:${address.port}")
    }

    fun loop() {
        runBlocking {
            while (true) {
                val clientConnection = serverSocket.accept().connection()
                launch {
                    logger.debug("Accepted socket connection from {}", clientConnection.socket.addressString)
                    try {
                        when (detectProtocol(clientConnection)) {
                            GatewayProtocol.Minecraft -> handleClient(Client(clientConnection))
                            GatewayProtocol.LegacyMinecraft -> Client(clientConnection).handleLegacyServerList()
                            GatewayProtocol.MinecraftTLS -> TODO("currently server-side tls in ktor-network is missing")
                            GatewayProtocol.MinecraftQuic -> error("QUIC is UDP based")
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        // client closed connection
                        logger.trace("{} closed connection", clientConnection.socket.addressString)
                    } catch (e: SocketTimeoutException) {
                        // socket timed out
                        logger.trace("{} timed out", clientConnection.socket.addressString)
                    } catch (e: IOException) {
                        // client closed connection
                        logger.trace("{} closed connection", clientConnection.socket.addressString)
                    } catch (e: Throwable) {
                        logger.error("unexpected error", e)
                    }
                    clientConnection.socket.close()
                }
            }
        }
    }

    private suspend fun detectProtocol(connection: Connection): GatewayProtocol {
        val peekedBytes = connection.input.peek(3) ?: throw ClosedReceiveChannelException("failed to detect protocol")
        return when (peekedBytes) {
            ByteString(byteArrayOf(0x16, 0x03, 0x04)) -> GatewayProtocol.MinecraftTLS // TLS1.3
            ByteString(byteArrayOf(0xfe.toByte(), 0x01, 0xfa.toByte())) -> GatewayProtocol.LegacyMinecraft
            else -> GatewayProtocol.Minecraft
        }
    }

    private suspend fun handleClient(client: Client) {
        val handshakePacket = client.readHandshakePacket()
        logger.trace("received {}", handshakePacket)
        client.state = handshakePacket.nextState
        while (true) {
            when (client.state) {
                State.Status -> when (val packet = client.readPacket()) {
                    is PacketInStatusStatusRequest -> client.handleStatusRequest()
                    is PacketInStatusPingRequest -> client.handlePing(packet)
                    else -> throw ProtocolException("unexpected packet in status state") // unreachable
                }
                State.Login, State.Transfer -> when (val packet = client.readPacket()) {
                    is PacketInLoginStartLogin -> client.login(handshakePacket, packet.name, packet.uuid, selectorManager, RoutingToken.Verifier.randomVerifier(), "eu-central-1-lobby", object: IServerIdResolver {
                        override fun resolveRoutingToken(token: RoutingToken): Pair<String, Int> {
                            return "localhost" to 25577
                        }
                    })
                    else -> throw ProtocolException("unexpected packet in login state") // unreachable
                }
                State.Proxied -> client.forwardData()
                else -> throw ProtocolException("invalid state")
            }
        }
    }

}

fun main() {
    IngressController().loop()
}
