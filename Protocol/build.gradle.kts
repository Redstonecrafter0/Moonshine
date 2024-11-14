plugins {
    id(libs.plugins.kotlin.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
}

group = "dev.redstones.moonshine.protocol"
version = libs.versions.moonshine.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Packet"))
    implementation(libs.ktor.network)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.adventure)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
