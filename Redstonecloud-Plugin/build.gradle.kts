import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("org.spongepowered.gradle.plugin") version "2.0.0"
    id("idea")
    id("org.jetbrains.dokka") version "1.6.10"
    `maven-publish`
}

idea {
    module {
        isDownloadSources = true
    }
}

val cloudVersion: String by project

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.spongepowered.org/maven")
    maven("https://repo.kryptonmc.org/releases")
}

dependencies {
    val kotlinVersion: String by System.getProperties()
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.springframework.boot:spring-boot-starter-web:2.6.7")
    implementation("org.slf4j:slf4j-jdk14:1.7.36")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("redis.clients:jedis:2.8.1")

    val bukkitVersion: String by project
    val bungeeVersion: String by project
    val spongeVersion: String by project
    val velocityVersion: String by project

    compileOnly("org.bukkit:bukkit:$bukkitVersion")
    compileOnly("net.md-5:bungeecord-api:$bungeeVersion")
    compileOnly("org.spongepowered:spongeapi:$spongeVersion")
    compileOnly("com.velocitypowered:velocity-api:$velocityVersion")
    kapt("com.velocitypowered:velocity-api:$velocityVersion")
}

sponge {
    val spongeVersion: String by project
    apiVersion(spongeVersion)
    license("AGPLv3")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("redstonecloud-plugin") {
        displayName("Redstonecloud-Plugin")
        entrypoint("net.redstonecraft.redstonecloud")
        description("Sponge integration for a cloud-native Minecraft network.")
        links {
            homepage("https://redstonecrafter0.github.io/Redstonecloud")
            source("https://github.com/Redstonecrafter0/Redstonecloud")
            issues("https://github.com/Redstonecrafter0/Redstonecloud/issues")
        }
        contributor("Redstonecrafter0") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
        version(cloudVersion)
    }
}

tasks {
    val javaVersion = JavaVersion.VERSION_1_8
    val javaVersionI = 8
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersionI)
    }
    withType<KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersionI))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
    processResources {
        expand("version" to cloudVersion)
    }
    dokkaHtmlPartial.configure {
        moduleName.set("Redstonecloud-Plugin")
        dokkaSourceSets {
            configureEach {
                includes.from("dokka-docs.md")
                jdkVersion.set(8)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("main") {
            groupId = "net.redstonecraft.redstonecloud"
            artifactId = "plugin"
            version = if (System.getenv("FINAL_RELEASE").toBoolean()) {
                cloudVersion
            } else {
                "$cloudVersion-${System.getenv("GITHUB_RUN_NUMBER")}"
            }
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Redstonecrafter0/Redstonecloud")
            credentials {
                username = "Redstonecrafter0"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
