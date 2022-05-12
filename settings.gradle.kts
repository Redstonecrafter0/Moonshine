rootProject.name = "Redstonecloud"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val kotlinVersion: String by System.getProperties()
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

include("Redstonecloud-Plugin")
