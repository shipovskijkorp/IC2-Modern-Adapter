import java.util.Properties
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

plugins {
    java
    id("fabric-loom") version "1.17.19"
}

val commonPropertiesFile = rootProject.file("../build.properties")
val commonProperties = Properties().apply {
    require(commonPropertiesFile.isFile) {
        "Missing shared build configuration: ${commonPropertiesFile.absolutePath}"
    }
    commonPropertiesFile.inputStream().use(::load)
}

fun commonProperty(name: String): String =
    commonProperties.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }
        ?: error("Missing shared build property '$name' in ${commonPropertiesFile.absolutePath}")

val modId = commonProperty("common.mod.id")
val modName = commonProperty("common.mod.name")
val modVersion = commonProperty("common.mod.version")
val modGroup = commonProperty("common.mod.group")
val modArchiveName = commonProperty("common.mod.archive_name")
val modAuthors = commonProperty("common.mod.authors")
val modLicense = commonProperty("common.mod.license")
val junitVersion = commonProperty("common.deps.junit")
val gsonVersion = commonProperty("common.deps.gson")
val sharedSourceRoot = commonProperty("common.source.shared_root")

val minecraftVersion = property("minecraft_version").toString()
val loaderVersion = property("fabric_loader_version").toString()
val fabricApiVersion = property("fabric_api_version").toString()
val brrpVersion = property("brrp_version").toString()
val javaVersion = property("java_version").toString().toInt()
val minecraftDependency = property("minecraft_dependency").toString()

version = "$modVersion+1.20.1-fabric"
group = modGroup

base {
    archivesName.set(modArchiveName)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

sourceSets {
    named("main") {
        java.srcDir(rootProject.file("../$sharedSourceRoot/main/java"))
        resources.srcDir(rootProject.file("../$sharedSourceRoot/main/resources"))
    }
    named("test") {
        java.srcDir(rootProject.file("../$sharedSourceRoot/test/java"))
        resources.srcDir(rootProject.file("../$sharedSourceRoot/test/resources"))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // BRRP publishes the converted original IC2 assets as an in-memory client resource pack.
    // It is nested into IC2MA so players only need Fabric API as an external dependency.
    modImplementation(include("maven.modrinth:JnrDtPAE:$brrpVersion")!!)

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.google.code.gson:gson:$gsonVersion")
}

tasks.processResources {
    val properties = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to project.version.toString(),
        "mod_authors" to modAuthors,
        "mod_license" to modLicense,
        "minecraft_dependency" to minecraftDependency,
        "loader_version" to loaderVersion,
        "java_version" to javaVersion
    )
    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Keep Loom's intermediary/dev jar away from the family artifact directory. The root collector
// must only see the remapped production jar.
tasks.named<Jar>("jar") {
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
    val bundledLicenseFile = rootProject.layout.projectDirectory.file("../LICENSE")
    val bundledLicenseName = "LICENSE_$modId"
    from(bundledLicenseFile) {
        rename("LICENSE", bundledLicenseName)
    }
}

tasks.register("buildAndCollect") {
    group = "build"
    description = "Builds the Fabric 1.20.1 artifact for the root collector."
    dependsOn(tasks.named("build"))
}
