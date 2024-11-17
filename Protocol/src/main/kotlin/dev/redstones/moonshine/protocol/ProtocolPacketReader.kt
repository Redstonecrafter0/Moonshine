package dev.redstones.moonshine.protocol

import dev.redstones.moonshine.protocol.packet.*
import dev.redstones.moonshine.packet.*
import dev.redstones.moonshine.packet.channel.LimitedSizeByteReadChannel
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

enum class ProtocolPacketReader(val direction: Direction, val state: State, val packetId: Int) {

    InHandshakingHandshake(Direction.Inbound, State.Handshaking, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket<Int> {
            return PacketInHandshakingHandshake(
                channel.readVarInt(),
                channel.readString(128),
                channel.readUShort().toInt(),
                State.fromId(channel.readVarInt()) ?: throw ProtocolException("invalid next state")
            )
        }
    },
    InStatusStatusRequest(Direction.Inbound, State.Status, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket<Int> {
            // this packet is empty
            return PacketInStatusStatusRequest
        }
    },
    InStatusPingRequest(Direction.Inbound, State.Status, 0x01) {
        override suspend fun read(channel: ByteReadChannel): IPacket<Int> {
            return PacketInStatusPingRequest(channel.readLong())
        }
    },
    InLoginStartLogin(Direction.Inbound, State.Login, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket<Int> {
            return PacketInLoginStartLogin(channel.readString(16), channel.readUuid())
        }
    },
    OutStatusStatusResponse(Direction.Outbound, State.Status, 0x00) {
        override suspend fun read(channel: ByteReadChannel): IPacket<Int> {
            return PacketOutStatusStatusResponse(channel.readString(4096))
        }
    },
    OutStatusPingResponse(Direction.Outbound, State.Status, 0x01) {
        override suspend fun read(channel: ByteReadChannel): IPacket<Int> {
            return PacketOutStatusPingResponse(channel.readLong())
        }
    }

    ;

    abstract suspend fun read(channel: ByteReadChannel): IPacket<Int>

    companion object {

        private val packets = buildMap {
            for (direction in enumValues<Direction>()) {
                set(direction, buildMap {
                    for (state in enumValues<State>()) {
                        set(state, buildMap {
                            enumValues<ProtocolPacketReader>().filter { it.state == state && it.direction == direction }.forEach {
                                set(it.packetId, it)
                            }
                        })
                    }
                })
            }
        }

        fun read(direction: Direction, stateProducer: ReceiveChannel<State>, receiveChannel: ByteReadChannel): Flow<IPacket<Int>> = flow {
            while (true) {
                val state = stateProducer.receive()
                if (state.stopReading) return@flow
                val channel = LimitedSizeByteReadChannel(receiveChannel, receiveChannel.readVarInt())
                val packetId = channel.readVarInt()
                val packet = packets[direction]?.get(state)?.get(packetId) ?: throw ProtocolException("Unsupported packet or state")
                emit(packet.read(channel))
            }
        }.flowOn(Dispatchers.IO)
    }

}
