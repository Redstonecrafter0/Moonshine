package dev.redstones.moonshine.protocol

/**
 * @param id used by handshake packet
 * */
enum class State(val id: Int, val stopReading: Boolean = false) {
    Handshaking(-1),
    Status(1),
    Login(2),
    Transfer(3),
    Play(-1),
    Proxied(-1, true), // custom for plain tcp proxying
    Close(-1, true); // custom for signalling a connection should close

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
