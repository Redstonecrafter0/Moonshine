package dev.redstones.moonshine.packet.channel

import io.ktor.utils.io.*
import kotlinx.io.EOFException

class LimitedSizeByteReadChannel(private val channel: ByteReadChannel, val size: Int): ByteReadChannel by channel {

    var pos = 0
        private set

    val isDone: Boolean
        get() = pos > size

    suspend fun readByte(): Byte {
        failSize(1)
        return channel.readByte()
    }

    suspend fun readShort(): Short {
        failSize(2)
        return channel.readShort()
    }

    suspend fun readInt(): Int {
        failSize(4)
        return channel.readInt()
    }

    suspend fun readLong(): Long {
        failSize(8)
        return channel.readLong()
    }

    private fun failSize(increment: Int) {
        pos += increment
        if (isDone) {
            throw EOFException("Packet size too small")
        }
    }

}
