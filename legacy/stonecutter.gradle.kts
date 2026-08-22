plugins {
    id("dev.kikugie.stonecutter")
    id("net.minecraftforge.gradle") version "6.0.54" apply false
}

stonecutter active "1.20.1-forge" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "build"
    description = "Builds every legacy Stonecutter target and collects its JAR."
    ofTask("buildAndCollect")
}
