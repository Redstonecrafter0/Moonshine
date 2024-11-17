package dev.redstones.moonshine.tls.protocol

import dev.redstones.moonshine.packet.channel.LimitedSizeByteReadChannel
import dev.redstones.moonshine.packet.readUByte
import dev.redstones.moonshine.packet.readUShort
import dev.redstones.moonshine.tls.CipherSuite
import io.ktor.utils.io.*

enum class TlsHandshakePacketReader(val packetId: UByte) {
    ClientHello(1u) {
        override suspend fun read(channel: ByteReadChannel): TlsTransportEvent {
            channel.readUShort() // legacy version
            val random = channel.readByteArray(32)
            val legacySessionIdLength = channel.readUByte()
            if (legacySessionIdLength !in 0..32) {
                return TlsTransportEvent.Alert()
            }
            channel.readByteArray(legacySessionIdLength.toInt()) // legacy session id
            val cipherSuitesLength = channel.readUShort().toInt() / 2
            if (cipherSuitesLength !in 2..65534) {
                return TlsTransportEvent.Alert()
            }
            val cipherSuites = buildSet {
                for (i in 0 until cipherSuitesLength) {
                    add(CipherSuite.byId[channel.readUShort()] ?: continue)
                }
            }
            val legacyCompressionMethodsLength = channel.readUByte()
            if (legacyCompressionMethodsLength != 1.toUByte()) {
                return TlsTransportEvent.Alert(TlsTransportEvent.Alert.AlertType.IllegalParameter)
            }
            if (channel.readUByte() != 0.toUByte()) {
                return TlsTransportEvent.Alert(TlsTransportEvent.Alert.AlertType.IllegalParameter)
            }
            channel.readByteArray(legacyCompressionMethodsLength.toInt()) // legacy compression method
            val extensionsSize = channel.readUShort()
            if (extensionsSize < 8) {
                return TlsTransportEvent.Alert()
            }
        }
    },
    ServerHello(2u),
    NewSessionTicket(4u),
    EndOfEarlyData(5u),
    EncryptedExtensions(8u),
    Certificate(11u),
    CertificateRequest(13u),
    CertificateVerify(15u),
    Finished(20u),
    KeyUpdate(24u),
    MessageHash(254u);

    abstract suspend fun read(channel: ByteReadChannel): TlsTransportEvent

    companion object {

        private val packets = buildMap {
            enumValues<TlsHandshakePacketReader>().forEach {
                set(it.packetId, it)
            }
        }

        suspend fun read(state: TlsConnectionState, receiveChannel: ByteReadChannel): TlsTransportEvent {
            while (true) {
                val packetId = receiveChannel.readUByte()
                val packet = packets[packetId]
                if (state.cipher != null && packet == ClientHello) {
                    return TlsTransportEvent.Alert(TlsTransportEvent.Alert.AlertType.UnexpectedMessage)
                }
                val size = receiveChannel.readUShort().toInt()
                val content = LimitedSizeByteReadChannel(receiveChannel, size)
                return packet.read(content)
            }
        }
    }

}
