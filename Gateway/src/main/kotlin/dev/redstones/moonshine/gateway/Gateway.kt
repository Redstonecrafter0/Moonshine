package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.common.util.addressString
import dev.redstones.moonshine.packet.ProtocolException
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.io.EOFException
import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException

class Gateway {

    companion object {
        val logger = LoggerFactory.getLogger(Gateway::class.java)
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
                launch(Dispatchers.IO) {
                    logger.debug("Accepted socket connection from {}", clientConnection.socket.addressString)
                    var client: BaseClient? = null
                    try {
                        try {
                            client = when (detectProtocol(clientConnection)) {
                                GatewayProtocol.Minecraft -> MinecraftClient(clientConnection, selectorManager)
                                GatewayProtocol.LegacyMinecraft -> LegacyClient(clientConnection, selectorManager)
                                GatewayProtocol.MinecraftTLS -> TODO("currently server-side tls in ktor-network is missing")
                                GatewayProtocol.MinecraftQuic -> error("QUIC is UDP based")
                            }
                            client.handleClientConnection()
                        } catch (e: ClosedReceiveChannelException) {
                            // client closed connection
                            logger.trace("${clientConnection.socket.addressString} closed connection")
                        } catch (e: EOFException) {
                            // client closed connection
                            logger.trace("${clientConnection.socket.addressString} closed connection")
                        } catch (e: SocketTimeoutException) {
                            logger.trace("${clientConnection.socket.addressString} timed out", e)
                        } catch (e: ProtocolException) {
                            logger.trace("protocol error", e)
                        } catch (e: IOException) {
                            // client closed connection
                            logger.trace("${clientConnection.socket.addressString} closed connection", e)
                        } catch (e: Throwable) {
                            logger.error("unexpected error", e)
                        }
                        client?.close() ?: clientConnection.socket.close()
                    } catch (e: Throwable) {
                        logger.error("crash prevented", e)
                    }
                }
            }
        }
    }

    private suspend fun detectProtocol(connection: Connection): GatewayProtocol {
        val peekedBytes = connection.input.peek(2) ?: throw ClosedReceiveChannelException("failed to detect protocol")
        return when (peekedBytes) {
            // technically the RFC8446 forbids processing the second byte here, however most tls clients use a valid tls version (which always starts with 0x03)
            // for backward compatibility. this will still always negotiate the use of tls1.3
            ByteString(byteArrayOf(0x16, 0x03)) -> GatewayProtocol.MinecraftTLS
            ByteString(byteArrayOf(0xFE.toByte(), 0x01)) -> GatewayProtocol.LegacyMinecraft
            else -> GatewayProtocol.Minecraft
        }
    }

}

fun main() {
    Gateway().loop()
}
