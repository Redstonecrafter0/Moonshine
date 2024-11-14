package dev.redstones.moonshine.gateway

import dev.redstones.moonshine.protocol.slp.ServerListPing
import dev.redstones.moonshine.protocol.slp.ServerListPinger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import java.io.Closeable

abstract class BaseClient(protected val connection: Connection, protected val selectorManager: SelectorManager): Closeable {

    val address: String
        get() = "${(connection.socket.remoteAddress as InetSocketAddress).hostname}:${(connection.socket.remoteAddress as InetSocketAddress).port}"

    val isClosed: Boolean by connection.socket::isClosed

    protected suspend fun pingDownstream(host: String, port: Int): ServerListPing {
        return ServerListPinger.ping("127.0.0.1", 25577) ?: ServerListPing(
            ServerListPing.Version("failed", 127),
            ServerListPing.Players(-1, 0, emptyList()),
        )
    }

    abstract suspend fun handleClientConnection()

    override fun close() {
        connection.socket.close()
    }

}
