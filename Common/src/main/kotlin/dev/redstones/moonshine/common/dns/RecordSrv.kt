package dev.redstones.moonshine.common.dns

data class RecordSrv(
    val priority: Int,
    val weight: Int,
    val port: Int,
    val address: String,
    val host: String
)
