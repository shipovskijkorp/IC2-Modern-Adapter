pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        version("1.21.1-neoforge", "1.21.1")
        vcsVersion = "1.21.1-neoforge"

        // Future modern targets stay in this Gradle 9.x family, e.g.:
        // version("26.1-neoforge", "26.1")
        // version("26.2-neoforge", "26.2")
    }
}

rootProject.name = "IC2-Modern-Adapter-Modern"
