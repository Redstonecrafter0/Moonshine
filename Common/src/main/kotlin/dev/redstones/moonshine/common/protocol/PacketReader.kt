package dev.redstones.moonshine.common.protocol

import dev.redstones.moonshine.common.protocol.packet.*
import dev.redstones.moonshine.common.protocol.channel.LimitedSizeByteReadChannel
import io.ktor.utils.io.*
import kotlinx.io.IOException

enum class PacketReader(val direction: Direction, val state: State, val packetId: Int) {

    InHandshakingHandshake(Direction.Inbound, State.Handshaking, 0x00) {
        override suspend fun read(channel: ByteReadChannel): PacketInHandshakingHandshake {
            return PacketInHandshakingHandshake(
                channel.readVarInt(),
                channel.readString(128),
                channel.readUShort(),
                State.fromId(channel.readVarInt()) ?: throw ProtocolException("invalid next state")
            )
        }
    },
    InStatusStatusRequest(Direction.Inbound, State.Status, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket {
            // this packet is empty
            return PacketInStatusStatusRequest
        }
    },
    InStatusPingRequest(Direction.Inbound, State.Status, 0x01) {
        override suspend fun read(channel: ByteReadChannel): IPacket {
            return PacketInStatusPingRequest(channel.readLong())
        }
    },
    InLoginStartLogin(Direction.Inbound, State.Login, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket {
            return PacketInLoginStartLogin(channel.readString(16), channel.readUuid())
        }
    },
    OutStatusStatusResponse(Direction.Outbound, State.Status, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket {
            return PacketOutStatusStatusResponse(channel.readString(4096))
        }
    },
    OutStatusPingResponse(Direction.Outbound, State.Status, 0x01) {
        override suspend fun read(channel: ByteReadChannel): IPacket {
            return PacketOutStatusPingResponse(channel.readLong())
        }
    }

    ;

    abstract suspend fun read(channel: ByteReadChannel): IPacket

    companion object {

        private val packets = buildMap {
            for (direction in enumValues<Direction>()) {
                set(direction, buildMap {
                    for (state in enumValues<State>()) {
                        set(state, buildMap {
                            enumValues<PacketReader>().filter { it.state == state && it.direction == direction }.forEach {
                                set(it.packetId, it)
                            }
                        })
                    }
                })
            }
        }

        suspend fun read(direction: Direction, state: State, receiveChannel: ByteReadChannel): IPacket {
            val channel = LimitedSizeByteReadChannel(receiveChannel, receiveChannel.readVarInt())
            val packetId = channel.readVarInt()
            val packet = packets[direction]?.get(state)?.get(packetId) ?: throw ProtocolException("Unsupported packet or state")
            return packet.read(channel)
        }
    }

}
