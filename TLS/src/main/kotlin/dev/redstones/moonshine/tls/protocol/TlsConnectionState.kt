package dev.redstones.moonshine.tls.protocol

import dev.redstones.moonshine.tls.ICipher

data class TlsConnectionState(
    val cipher: ICipher? = null
) {
}
