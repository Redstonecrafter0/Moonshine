package dev.redstones.moonshine.packet

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import java.util.UUID

private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

// modified from https://wiki.vg/Data_types#VarInt_and_VarLong
suspend fun ByteReadChannel.readVarInt(firstByte: Byte? = null): Int {
    var value = 0
    var position = 0
    var currentByte: Int = firstByte?.toInt() ?: readByte().toInt()

    while (true) {
        value = value or ((currentByte and SEGMENT_BITS) shl position)

        if ((currentByte and CONTINUE_BIT) == 0) break

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
        if ((value and SEGMENT_BITS.inv()) == 0) {
            writeByte(value.toByte())
            return
        }

        writeByte(((value and SEGMENT_BITS) or CONTINUE_BIT).toByte())

        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        value = value ushr 7
    }
}

suspend fun ByteReadChannel.readString(maxSize: Int = 32764): String {
    val size = readVarInt()
    if (size > maxSize) {
        throw ProtocolException("String too big")
    }
    return readByteArray(size).decodeToString()
}

suspend fun ByteWriteChannel.writeString(string: ByteArray) {
    writeVarInt(string.size)
    writeByteArray(string)
}

suspend fun ByteReadChannel.readUShort(): UShort {
    return readShort().toUShort()
}

suspend fun ByteWriteChannel.writeUShort(short: Int) {
    writeShort(short.toUShort().toShort())
}

suspend fun ByteReadChannel.readUByte(): UByte {
    return readByte().toUByte()
}

suspend fun ByteWriteChannel.writeUByte(byte: UByte) {
    writeByte(byte.toByte())
}

enum class PacketLengthPrefix(val write: suspend ByteWriteChannel.(Int) -> Unit) {
    VarInt(ByteWriteChannel::writeVarInt)
}

@OptIn(InternalAPI::class)
suspend fun ByteWriteChannel.writeWithPrefixedLength(lengthPrefix: PacketLengthPrefix, block: suspend ByteWriteChannel.() -> Unit): Int {
    val tmpBuffer = ByteChannel(false)
    tmpBuffer.block()
    tmpBuffer.flushWriteBuffer()
    val size = tmpBuffer.availableForRead
    lengthPrefix.write(this, size)
    return tmpBuffer.readBuffer.transferTo(writeBuffer).toInt()
}

@OptIn(InternalAPI::class)
suspend fun ByteArray.toByteReadChannel(): ByteReadChannel {
    val byteChannel = ByteChannel(false)
    byteChannel.writeByteArray(this)
    byteChannel.flushWriteBuffer()
    return byteChannel
}

suspend fun ByteReadChannel.readUuid(): UUID {
    return UUID(readLong(), readLong())
}

suspend fun ByteWriteChannel.writeUuid(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

suspend fun ByteReadChannel.readLegacyString(maxSize: Int = Short.MAX_VALUE.toInt()): String {
    val charSize = readUShort().toInt()
    if (charSize > maxSize) {
        throw ProtocolException("String too big")
    }
    return String(readByteArray(charSize * 2), Charsets.UTF_16BE) // all legal domain names and ip addresses have only 2 bytes per char
}

suspend fun ByteWriteChannel.writeLegacyString(string: String) {
    writeUShort(string.length)
    writeByteArray(string.toByteArray(Charsets.UTF_16BE))
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
