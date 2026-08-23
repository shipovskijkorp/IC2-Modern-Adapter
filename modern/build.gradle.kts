import java.util.Properties
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test

plugins {
    java
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev")
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
val jeiProject = commonProperty("common.deps.modrinth.jei_project")
val jeiVersion = commonProperty("common.deps.modrinth.jei_1_21_1_neoforge")
val sharedSourceRoot = commonProperty("common.source.shared_root")

val minecraftVersion = property("minecraft_version").toString()
val minecraftVersionRange = property("minecraft_version_range").toString()
val neoForgeVersion = property("neo_version").toString()
val loaderVersionRange = property("loader_version_range").toString()
val javaVersion = property("java_version").toString().toInt()
val packFormat = property("pack_format").toString().toInt()

version = "$modVersion+${stonecutter.current.project}"
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

neoForge {
    version = neoForgeVersion

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    unitTest {
        enable()
        testedMod = mods[modId]
    }
}

repositories {
    mavenCentral()
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
    compileOnly("maven.modrinth:$jeiProject:$jeiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.google.code.gson:gson:$gsonVersion")
}

tasks.processResources {
    val properties = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to project.version.toString(),
        "mod_authors" to modAuthors,
        "mod_license" to modLicense,
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "neo_version" to neoForgeVersion,
        "loader_version_range" to loaderVersionRange,
        "pack_format" to packFormat
    )

    inputs.properties(properties)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val bundledLicenseFile = rootProject.layout.projectDirectory.file("../LICENSE")
val bundledLicenseName = "LICENSE_$modId"

tasks.named<Jar>("jar") {
    // Keep the copy spec configuration-cache safe: do not capture the Kotlin build-script
    // object from a rename lambda. Both values are resolved during configuration instead.
    from(bundledLicenseFile) {
        rename("LICENSE", bundledLicenseName)
    }
}

tasks.register<Copy>("buildAndCollect") {
    dependsOn(tasks.named("build"))
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
}
