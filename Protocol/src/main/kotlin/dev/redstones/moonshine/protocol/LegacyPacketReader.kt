package dev.redstones.moonshine.protocol

import dev.redstones.moonshine.protocol.packet.*
import dev.redstones.moonshine.packet.*
import io.ktor.utils.io.*

enum class LegacyPacketReader(val direction: Direction, val packetId: UByte) {

    InServerListPing(Direction.Inbound, 0xFEu) {
        override suspend fun read(channel: ByteReadChannel): IPacket<UByte> {
            return LegacyPacketInServerListPing(channel.readUByte())
        }
    },
    InPluginMessage(Direction.Inbound, 0XFAu) {
        override suspend fun read(channel: ByteReadChannel): IPacket<UByte> {
            return LegacyPacketInPluginMessage(
                channel.readLegacyString(11), // only 'MC|PingHost' is accepted for this implementation
                channel.readByteArray(channel.readUShort())
            )
        }
    },
    OutKick(Direction.Outbound, 0xFFu) {
        override suspend fun read(channel: ByteReadChannel): IPacket<UByte> {
            return LegacyPacketOutKick(channel.readLegacyString())
        }
    }

    ;

    abstract suspend fun read(channel: ByteReadChannel): IPacket<UByte>

    companion object {

        private val packets = buildMap {
            for (direction in enumValues<Direction>()) {
                set(direction, buildMap {
                    enumValues<LegacyPacketReader>().filter { it.direction == direction }.forEach {
                        set(it.packetId, it)
                    }
                })
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        suspend fun read(direction: Direction, receiveChannel: ByteReadChannel): IPacket<UByte> {
            val packetId = receiveChannel.readUByte()
            val packet = packets[direction]?.get(packetId)
                ?: throw ProtocolException("Unsupported ${direction.name.lowercase()} packet ${packetId.toHexString()}")
            return packet.read(receiveChannel)
        }
    }

}
