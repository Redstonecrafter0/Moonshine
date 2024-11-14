package dev.redstones.moonshine.packet

import io.ktor.utils.io.*

interface IPacket<T> {

    val packetId: T

    suspend fun write(channel: ByteWriteChannel)

}
