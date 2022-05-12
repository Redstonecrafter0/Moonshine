plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.dokka") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    dokkaPlugin("com.glureau:html-mermaid-dokka-plugin:0.3.1")
}

tasks {
    dokkaHtmlMultiModule.configure {
        includes.from("README.md")
    }
}
