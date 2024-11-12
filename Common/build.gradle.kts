plugins {
    id(libs.plugins.kotlin.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
}

group = "dev.redstones.moonshine.common"
version = libs.versions.moonshine.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.bouncycastle)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.network)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.adventure)
    implementation(libs.apache.commons.validator)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
