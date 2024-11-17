package dev.redstones.moonshine.tls.protocol

import dev.redstones.moonshine.packet.IPacket
import io.ktor.utils.io.*

data class TlsHandshakeClientHello(): IPacket<UByte> {

    override val packetId: UByte = TlsHandshakePacketReader.ClientHello.packetId

    override suspend fun write(channel: ByteWriteChannel) {
    }

}
