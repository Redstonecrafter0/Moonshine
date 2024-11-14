package dev.redstones.moonshine.protocol

/**
 * @param id used by handshake packet
 * */
enum class State(val id: Int) {
    Handshaking(-1),
    Status(1),
    Login(2),
    Transfer(3),
    Play(-1),
    Proxied(-1); // custom for plain tcp proxying

    companion object {
        fun fromId(id: Int): State? {
            return when (id) {
                1 -> Status
                2 -> Login
                3 -> Transfer
                else -> null
            }
        }
    }

}
