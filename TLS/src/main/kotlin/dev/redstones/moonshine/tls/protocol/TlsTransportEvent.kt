package dev.redstones.moonshine.tls.protocol

import dev.redstones.moonshine.packet.IPacket

sealed class TlsTransportEvent {

    data class Packet(val packet: IPacket<UByte>): TlsTransportEvent()
    data class Alert(val alert: AlertType): TlsTransportEvent() {
        enum class AlertType(val id: String) {
            RecordOverflow("record_overflow"),
            UnexpectedMessage("unexpected_message"),
            HandshakeFailure("handshake_failure"),
            InsufficientSecurity("insufficient_security")
        }
    }

}
