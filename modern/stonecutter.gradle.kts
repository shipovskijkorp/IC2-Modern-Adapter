plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.143" apply false
}

stonecutter active "1.21.1-neoforge" /* [SC] DO NOT EDIT */

// Stonecutter 0.8 keeps the native task fan-out introduced in 0.7.
// Use the same `buildAndCollect` entry point as the legacy family.
