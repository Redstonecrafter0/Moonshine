package dev.redstones.moonshine.protocol.packet

import dev.redstones.moonshine.packet.*
import dev.redstones.moonshine.protocol.*
import io.ktor.utils.io.*
import java.util.UUID

data class PacketInHandshakingHandshake(val protocolVersion: Int, val host: String, val port: Int, val nextState: State): IPacket<Int> {

    override val packetId: Int = ProtocolPacketReader.InHandshakingHandshake.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        val bytes = host.encodeToByteArray()
        channel.writeWithPrefixedLength(PacketLengthPrefix.VarInt) {
            writeVarInt(packetId)
            writeVarInt(protocolVersion)
            writeString(bytes)
            writeUShort(port)
            writeVarInt(nextState.id)
        }
        channel.flush()
    }
}

data object PacketInStatusStatusRequest: IPacket<Int> {

    override val packetId: Int = ProtocolPacketReader.InStatusStatusRequest.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeWithPrefixedLength(PacketLengthPrefix.VarInt) {
            writeVarInt(packetId)
        }
        channel.flush()
    }
}

data class PacketInStatusPingRequest(val payload: Long): IPacket<Int> {

    override val packetId: Int = ProtocolPacketReader.InStatusPingRequest.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeWithPrefixedLength(PacketLengthPrefix.VarInt) {
            writeVarInt(packetId)
            writeLong(payload)
        }
        channel.flush()
    }
}

data class PacketInLoginStartLogin(val name: String, val uuid: UUID): IPacket<Int> {

    override val packetId: Int = ProtocolPacketReader.InLoginStartLogin.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        val bytes = name.encodeToByteArray()
        channel.writeWithPrefixedLength(PacketLengthPrefix.VarInt) {
            writeVarInt(packetId)
            writeString(bytes)
            writeUuid(uuid)
        }
        channel.flush()
    }
}


data class PacketOutStatusStatusResponse(val jsonResponse: String): IPacket<Int> {

    override val packetId: Int = ProtocolPacketReader.OutStatusStatusResponse.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        val string = jsonResponse.encodeToByteArray()
        channel.writeWithPrefixedLength(PacketLengthPrefix.VarInt) {
            writeVarInt(packetId)
            writeString(string)
        }
        channel.flush()
    }

}

data class PacketOutStatusPingResponse(val payload: Long): IPacket<Int> {

    override val packetId: Int = ProtocolPacketReader.OutStatusPingResponse.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeWithPrefixedLength(PacketLengthPrefix.VarInt) {
            writeVarInt(packetId)
            writeLong(payload)
        }
        channel.flush()
    }

}

data class LegacyPacketInServerListPing(val payload: UByte): IPacket<UByte> {

    override val packetId: UByte = LegacyPacketReader.InServerListPing.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeByte(packetId.toByte())
        channel.writeUByte(payload)
    }

}

data class LegacyPacketInPluginMessage(val id: String, val payload: ByteArray): IPacket<UByte> {

    override val packetId: UByte = LegacyPacketReader.InPluginMessage.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeByte(packetId.toByte())
        channel.writeLegacyString(id)
        channel.writeUShort(payload.size)
        channel.writeByteArray(payload)
    }

    data class LegacyPingHost(val protocolVersion: UByte, val host: String, val port: Int)

    @OptIn(InternalAPI::class)
    suspend fun decodePingHostMessage(): LegacyPingHost? {
        if (id != "MC|PingHost") return null
        val channel = ByteChannel(false)
        channel.writeByteArray(payload)
        channel.flush()
        channel.flushWriteBuffer()
        return LegacyPingHost(channel.readUByte(), channel.readLegacyString(), channel.readInt())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LegacyPacketInPluginMessage

        if (id != other.id) return false
        if (!payload.contentEquals(other.payload)) return false
        if (packetId != other.packetId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + packetId.hashCode()
        return result
    }

}

data class LegacyPacketOutKick(val message: String): IPacket<UByte> {

    /**
     * constructor for legacy server list ping packet
     * */
    constructor(protocolVersion: Int, serverVersion: String, motd: String, playerCount: Int, maxPlayerCount: Int):
        this("§1\u0000$protocolVersion\u0000$serverVersion\u0000$motd\u0000$playerCount\u0000$maxPlayerCount")

    override val packetId: UByte = LegacyPacketReader.OutKick.packetId

    override suspend fun write(channel: ByteWriteChannel) {
        channel.writeByte(packetId.toByte())
        channel.writeLegacyString(message)
        channel.flush()
    }

}
