plugins {
    id(libs.plugins.kotlin.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    id(libs.plugins.kapt.get().pluginId)
    idea
}

idea {
    module {
        isDownloadSources = true
    }
}

group = "dev.redstones.moonshine.lunarlink"
version = libs.versions.moonshine.get()

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8", libs.versions.kotlin.get()))

    implementation(project(":Common"))

    implementation(libs.gson)
    implementation(libs.jedis)

    implementation(libs.protocollib)

    compileOnly(libs.luckperms.api)

    compileOnly(libs.bundles.pluginapis)

    kapt(libs.velocity)
}

kotlin {
    jvmToolchain(8)
}

tasks {
    processResources {
        expand("version" to libs.versions.moonshine.get())
    }
}
