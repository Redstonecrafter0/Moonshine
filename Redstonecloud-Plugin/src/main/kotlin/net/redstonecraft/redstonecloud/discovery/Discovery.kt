package net.redstonecraft.redstonecloud.discovery

import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

class MulticastServer(address: String, port: Int, bufferSize: Int = 256, private val block: (String, InetAddress) -> Unit): Thread(), Closeable {

    private val socket = MulticastSocket(port)
    private val group = InetAddress.getByName(address)
    private val buffer = ByteArray(bufferSize)
    private var running = true

    override fun run() {
        socket.joinGroup(group)
        while (running) {
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            block(String(packet.data, 0, packet.length), packet.address)
        }
        socket.leaveGroup(group)
        socket.close()
    }

    override fun close() {
        running = false
    }
}

class UDPServer(port: Int, bufferSize: Int = 256, private val block: (String, InetAddress) -> Unit): Thread(), Closeable {

    private val socket = DatagramSocket(port)
    private val buffer = ByteArray(bufferSize)
    private var running = true

    override fun run() {
        while (running) {
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            block(String(packet.data, 0, packet.length), packet.address)
        }
        socket.close()
    }

    override fun close() {
        running = false
    }

}

class UDPClient(private val address: InetAddress, private val port: Int): Closeable {

    private val socket = DatagramSocket()

    fun send(msg: String, block: ((String) -> Unit)? = null) {
        val buffer = msg.encodeToByteArray()
        val packet = DatagramPacket(buffer, buffer.size, address, port)
        socket.send(packet)
        if (block != null) {
            val buffer = ByteArray(32768)
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            block(String(packet.data, 0, packet.length))
        }
    }

    override fun close() = socket.close()

}
