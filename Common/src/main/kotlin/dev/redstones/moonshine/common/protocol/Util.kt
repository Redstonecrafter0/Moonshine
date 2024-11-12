package dev.redstones.moonshine.common.protocol

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import java.nio.charset.CodingErrorAction
import java.util.UUID


private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

// modified from https://wiki.vg/Data_types#VarInt_and_VarLong
suspend fun ByteReadChannel.readVarInt(firstByte: Byte? = null): Int {
    var value = 0
    var position = 0
    var currentByte: Int = firstByte?.toInt() ?: readByte().toInt()

    while (true) {
        value = value or ((currentByte and dev.redstones.moonshine.common.protocol.SEGMENT_BITS) shl position)

        if ((currentByte and dev.redstones.moonshine.common.protocol.CONTINUE_BIT) == 0) break

        position += 7

        if (position >= 32) throw ProtocolException("VarInt too big")

        currentByte = readByte().toInt()
    }

    return value
}

// modified from https://wiki.vg/Data_types#VarInt_and_VarLong
suspend fun ByteWriteChannel.writeVarInt(value: Int) {
    var value = value
    while (true) {
        if ((value and dev.redstones.moonshine.common.protocol.SEGMENT_BITS.inv()) == 0) {
            writeByte(value.toByte())
            return
        }

        writeByte(((value and dev.redstones.moonshine.common.protocol.SEGMENT_BITS) or dev.redstones.moonshine.common.protocol.CONTINUE_BIT).toByte())

        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        value = value ushr 7
    }
}

suspend fun ByteReadChannel.readString(maxSize: Int = 32764): String {
    val size = readVarInt()
    if (size > maxSize) {
        throw ProtocolException("String too big")
    }
    return ByteArray(size) { readByte() }.decodeToString()
}

suspend fun ByteWriteChannel.writeString(string: ByteArray) {
    writeVarInt(string.size)
    for (i in string) {
        writeByte(i)
    }
}

suspend fun ByteReadChannel.readUShort(): Int {
    return readShort().toUShort().toInt()
}

suspend fun ByteWriteChannel.writeUShort(short: Int) {
    writeShort(short.toUShort().toShort())
}

suspend fun ByteWriteChannel.writeHead(packet: PacketReader, size: Int) {
    writeVarInt(size + packet.packetId.getVarIntSize())
    writeVarInt(packet.packetId)
}

suspend fun ByteWriteChannel.writeHead(packetId: Int, size: Int) {
    writeVarInt(size + packetId.getVarIntSize())
    writeVarInt(packetId)
}

suspend fun ByteReadChannel.readUuid(): UUID {
    return UUID(readLong(), readLong())
}

suspend fun ByteWriteChannel.writeUuid(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

suspend fun ByteReadChannel.legacyReadString(maxSize: Int = Short.MAX_VALUE.toInt()): String {
    val charSize = readShort()
    if (charSize > maxSize) {
        throw ProtocolException("String too big")
    }
    return String(ByteArray(charSize * 2) { readByte() }, Charsets.UTF_16BE) // all legal domain names and ip addresses have only 2 bytes per char
}

suspend fun ByteWriteChannel.legacyWriteString(string: String) {
    writeShort(string.length.toShort())
    for (i in string.toByteArray(Charsets.UTF_16BE)) {
        writeByte(i)
    }
}

fun Int.getVarIntSize(): Int {
    var size = 0
    var value = this
    while (true) {
        if ((value and SEGMENT_BITS.inv()) == 0) {
            size++
            return size
        }

        size++

        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        value = value ushr 7
    }
}
