package dev.redstones.moonshine.tls.protocol

import dev.redstones.moonshine.packet.channel.LimitedSizeByteReadChannel
import dev.redstones.moonshine.packet.readUByte
import dev.redstones.moonshine.packet.readUShort
import dev.redstones.moonshine.packet.toByteReadChannel
import io.ktor.utils.io.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class TlsRecordPacketReader(val packetId: UByte) {
    Invalid(0u),
    ChangeCipherSpec(20u),
    Alert(21u),
    Handshake(22u) {
        override suspend fun read(state: TlsConnectionState, channel: ByteReadChannel): TlsTransportEvent {
            return TlsHandshakePacketReader.read(state, channel)
        }
    },
    ApplicationData(23u);

    abstract suspend fun read(state: TlsConnectionState, channel: ByteReadChannel): TlsTransportEvent

    companion object {

        private val packets = buildMap {
            enumValues<TlsRecordPacketReader>().forEach {
                set(it.packetId, it)
            }
        }

        private fun getPacketById(packetId: UByte) = packets.getOrDefault(packetId, Invalid)

        fun read(receiveChannel: ByteReadChannel, stateProducer: ReceiveChannel<TlsConnectionState>): Flow<TlsTransportEvent> = flow {
            while (true) {
                val state = stateProducer.receive()
                val packetId = receiveChannel.readUByte()
                var packet = getPacketById(packetId)
                receiveChannel.readUShort() // legacyRecordVersion
                val size = receiveChannel.readUShort().toInt()
                if (state.cipher == null && size > 0b100000000000000) {
                    emit(TlsTransportEvent.Alert(TlsTransportEvent.Alert.AlertType.RecordOverflow))
                    return@flow
                } else if (size > 0b100000100000000) {
                    emit(TlsTransportEvent.Alert(TlsTransportEvent.Alert.AlertType.RecordOverflow))
                    return@flow
                }
                if (state.cipher == null && packet == ApplicationData) {
                    emit(TlsTransportEvent.Alert())
                    return@flow
                }
                val content = if (state.cipher != null) {
                    val decryptedData = state.cipher.decrypt(receiveChannel.readByteArray(size))
                    var decryptedSize = 0
                    for (i in decryptedData.indices.reversed()) {
                        if (decryptedData[i] != 0.toByte()) {
                            decryptedSize = i
                            break
                        }
                    }
                    packet = getPacketById(decryptedData[decryptedSize].toUByte())
                    val content = ByteArray(decryptedSize)
                    System.arraycopy(decryptedData, 0, content, 0, decryptedSize)
                    content.toByteReadChannel()
                } else {
                    LimitedSizeByteReadChannel(receiveChannel, size)
                }
                emit(TlsTransportEvent.Packet(packet.read(state, content)))
            }
        }
    }

}
