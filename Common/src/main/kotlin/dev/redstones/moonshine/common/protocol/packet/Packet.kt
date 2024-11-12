package dev.redstones.moonshine.common.protocol.packet

import dev.redstones.moonshine.common.protocol.*
import io.ktor.utils.io.*
import java.util.UUID

data class PacketInHandshakingHandshake(val protocolVersion: Int, val host: String, val port: Int, val nextState: State):
    IPacket {
    override suspend fun write(channel: ByteWriteChannel) {
        val bytes = host.encodeToByteArray()
        channel.writeHead(
            PacketReader.InHandshakingHandshake,
            protocolVersion.getVarIntSize() + bytes.size.getVarIntSize() + bytes.size + 2 + nextState.id.getVarIntSize()
        )
        channel.writeVarInt(protocolVersion)
        channel.writeString(bytes)
        channel.writeUShort(port)
        channel.writeVarInt(nextState.id)
        channel.flush()
    }
}

data object PacketInStatusStatusRequest: IPacket {
    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeHead(PacketReader.InStatusStatusRequest, 0)
        channel.flush()
    }
}
data class PacketInStatusPingRequest(val payload: Long): IPacket {
    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeHead(PacketReader.InStatusPingRequest, 8)
        channel.writeLong(payload)
        channel.flush()
    }
}

data class PacketInLoginStartLogin(val name: String, val uuid: UUID): IPacket {
    override suspend fun write(channel: ByteWriteChannel) {
        val bytes = name.encodeToByteArray()
        channel.writeHead(PacketReader.InLoginStartLogin, bytes.size.getVarIntSize() + bytes.size + 16)
        channel.writeString(bytes)
        channel.writeUuid(uuid)
        channel.flush()
    }
}


data class PacketOutStatusStatusResponse(val jsonResponse: String): IPacket {

    override suspend fun write(channel: ByteWriteChannel) {
        val string = jsonResponse.encodeToByteArray()
        channel.writeHead(0x00, string.size.getVarIntSize() + string.size)
        channel.writeString(string)
        channel.flush()
    }

}
data class PacketOutStatusPingResponse(val payload: Long): IPacket {

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeHead(0x01, 8)
        channel.writeLong(payload)
        channel.flush()
    }

}
