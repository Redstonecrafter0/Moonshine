package dev.redstones.moonshine.common.util

import io.ktor.network.sockets.*
import org.bouncycastle.util.encoders.Base64Encoder
import java.io.ByteArrayOutputStream

fun ByteArray.toBase64(): String {
    val encoder = Base64Encoder()
    val length = encoder.getEncodedLength(size)
    val output = ByteArray(length)
    encoder.encode(this, 0, size, output, 0)
    return output.decodeToString()
}

fun String.fromBase64ToByteArray(): ByteArray {
    val decoder = Base64Encoder()
    val maxLength = decoder.getMaxDecodedLength(length)
    val outputStream = ByteArrayOutputStream(maxLength)
    decoder.decode(this, outputStream)
    return outputStream.toByteArray()
}

val Socket.addressString: String
    get() = "${(remoteAddress as InetSocketAddress).hostname}:${(remoteAddress as InetSocketAddress).port}"
