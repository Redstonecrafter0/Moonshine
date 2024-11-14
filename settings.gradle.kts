plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Moonshine"
include("Gateway")
include("LunarLink")
include("Common")
include("QUIC")
include("TLS")
include("Protocol")
include("Packet")
