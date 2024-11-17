package dev.redstones.moonshine.tls

interface ICipher {

    fun encrypt(data: ByteArray): ByteArray

    fun decrypt(data: ByteArray): ByteArray

}
