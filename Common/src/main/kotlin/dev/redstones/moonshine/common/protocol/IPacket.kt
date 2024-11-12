package dev.redstones.moonshine.common.protocol

import io.ktor.utils.io.*

interface IPacket {
    suspend fun write(channel: ByteWriteChannel)
}
