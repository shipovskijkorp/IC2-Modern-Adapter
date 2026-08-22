pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    // Stonecutter 0.8+ requires Gradle 9; the Forge 1.20.1 family is intentionally pinned to Gradle 8.8.
    id("dev.kikugie.stonecutter") version "0.7.11"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        version("1.20.1-forge", "1.20.1")
        vcsVersion = "1.20.1-forge"
    }
}

rootProject.name = "IC2-Modern-Adapter-Legacy"
