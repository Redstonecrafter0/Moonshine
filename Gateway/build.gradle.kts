plugins {
    id(libs.plugins.kotlin.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.shadow)
    application
}

group = "dev.redstones.moonshine.ingress"
version = libs.versions.moonshine.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Common"))
    implementation(project(":Protocol"))
    implementation(project(":Packet"))
//    implementation(libs.bundles.ktor.server)
//    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.network)
    implementation(libs.bundles.logging)
    implementation(libs.kotlinx.serialization.json)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.redstones.moonshine.gateway.GatewayKt")
}

tasks.shadowJar {
    minimize {
        exclude(dependency("${libs.log4j2.slf4j.get().group}:.*:.*"))
    }
}
