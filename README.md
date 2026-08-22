# IC2 Modern Adapter

Initial dual-family Stonecutter build scaffold.

## Shared build configuration

Repository-wide metadata and source layout are defined once in `build.properties`:

```properties
common.mod.group=com.shipovskijkorp.ic2modernadapter
common.mod.id=ic2_modern_adapter
common.mod.name=IC2 Modern Adapter
common.mod.version=0.0.0-dev
common.mod.archive_name=IC2-Modern-Adapter
common.mod.authors=ShipovskijKorp
common.mod.license=MPL-2.0
common.deps.junit=5.11.4
common.source.shared_root=source-shared

sourceFamilies=legacy,modern
behaviorReference=1.21.1-neoforge
```

Both Gradle families read mod metadata, the archive name, license, authors, JUnit version, and shared source root from this file. `sourceFamilies` also drives the repository-level build scripts.

`source-shared/main/java` and `source-shared/main/resources` are appended to the `main` source set of both families. Loader-specific bootstrap and metadata remain in the corresponding family source root.

## Build families

- `legacy/` — Minecraft 1.20.1 + Forge 47.4.11, Java 17, Gradle 8.8.
- `modern/` — Minecraft 1.21.1 + NeoForge 21.1.244, Java 21, Gradle 9.2.1.

The families are intentionally separate Gradle roots. The 1.20.1 Forge toolchain therefore cannot constrain the modern family when it moves to later Minecraft / NeoForge releases such as 26.x.

Each family is controlled by Stonecutter even though it currently contains a single target. Add future modern targets to `modern/settings.gradle.kts` and per-target values under `modern/versions/<target>/gradle.properties`.

`behaviorReference` identifies the canonical gameplay/behavior target for cross-version parity checks. It is metadata for future validation tooling and does not currently alter compilation.

## Build everything

Windows:

```powershell
./build-all.ps1
```

POSIX:

```sh
./build-all.sh
```

The scripts read `sourceFamilies` and build every configured family with its own Gradle wrapper. Artifacts are collected into `build/libs/`.

The tiny `gradlew` bootstrap scripts download the matching official Gradle wrapper JAR on first use. This keeps the initial scaffold text-only; after the first successful setup, the wrappers can be regenerated with the normal Gradle `wrapper` task and committed if desired.
