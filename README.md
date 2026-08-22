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

## Original IC2 runtime resources

IC2 Modern Adapter does not bundle IndustrialCraft² assets. Put a supported original IC2 archive at:

```text
.minecraft/
└── ic2-original/
    └── industrialcraft-2-2.8.222-ex112.jar
```

The archive is opened only as a read-only ZIP resource source. IC2MA never loads, links, reflects over, transforms, decompiles, or executes original IC2 classes.

For the current visual-placeholder milestone, the client resource pack is compiled entirely in memory:

- compatible original textures, models, animation metadata, sounds, and other client assets are exposed from the source archive;
- legacy Forge-marker blockstates are converted to ordinary modern blockstate JSON;
- original model references are remapped to the modern `block/` model namespace;
- legacy meta/NBT block variants are mapped to the finite `variant` blockstate used by IC2MA;
- `ic2:te` additionally preserves six-way facing for its machine models;
- fluid placeholders use their original animated still textures with translucent rendering;
- transparent IC2 block models are assigned a cutout render type by inspecting the alpha channel of their original PNG textures;
- code-side IC2 tint multipliers needed by the original models are restored for rubber leaves and the colored TE storage-box variants;
- generated JSON and copied asset bytes stay in memory; no converted resource pack is written to disk.

All currently registered block identities are placeable visual placeholders. They intentionally do not implement IC2 machine, energy, fluid, crop, redstone, or other gameplay behavior yet. `ic2:dynamite` is temporarily made placeable even though its 1.12 item was not a normal `BlockItem`, solely so its block model can be inspected during this milestone.

Legacy `lang_ic2/*.properties` translation conversion is intentionally deferred to the next resource-compatibility step; the runtime resource layer is structured so localization and any other format-specific conversions can be added without bundling original assets.
