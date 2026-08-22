plugins {
    id("dev.kikugie.stonecutter")
    id("net.minecraftforge.gradle") version "6.0.54" apply false
}

stonecutter active "1.20.1-forge" /* [SC] DO NOT EDIT */

// Stonecutter 0.7+ fans native Gradle tasks out to every configured node.
// Run `buildAndCollect` directly instead of the removed chiseled task API.
