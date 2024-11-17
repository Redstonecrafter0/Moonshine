package dev.redstones.moonshine.tls

enum class CipherSuite(val displayName: String, val id: UShort) {
    ;

    companion object {

        val byId = buildMap {
            enumValues<CipherSuite>().forEach {
                set(it.id, it)
            }
        }

    }

}
