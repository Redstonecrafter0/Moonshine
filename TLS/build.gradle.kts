plugins {
    id(libs.plugins.kotlin.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
}

group = "dev.redstones.moonshine.tls"
version = libs.versions.moonshine.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.bouncycastle)
    implementation(libs.ktor.network)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
