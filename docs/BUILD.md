# Building Limitless Skills (Minecraft 26.3)

## Requirements

- JDK 25 to run Gradle and compile the mod. On Windows, Eclipse Temurin 25 is one option. Set `JAVA_HOME` to the JDK 25 directory and put its `bin` directory first on `PATH`.
- Internet access on the first build for Gradle, Minecraft, Fabric, and UI Lib artifacts.
- No system Gradle installation is needed; the repository includes Windows and Unix wrappers pinned to Gradle 9.7.1.

All dependency versions are pinned in `build.gradle` and `gradle.properties`: Fabric Loom 1.18.2, Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, and UI Lib Fabric 22.1.0 (Modrinth version ID `ALJXpLCo`). The version ID selects the Fabric artifact specifically. The current development mod version is `2.0.3-phase1.0`; Gradle substitutes this into the packaged `fabric.mod.json`.

## Commands

Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Unix:

```sh
export JAVA_HOME=/path/to/jdk-25
./gradlew build
./gradlew runClient
./gradlew runServer
```

The distributable jar is `build/libs/limitless-skills-2.0.3-phase1.0.jar`. Do not install the `-sources.jar` file as a mod. The wrapper downloads Gradle 9.7.1 on first use. `runClient` downloads Minecraft assets on first use and can take several minutes. `runServer` requires acceptance of Mojang's EULA in the generated `run/eula.txt` before it will fully start; review the EULA yourself before setting `eula=true`.

Loom compiles only `src/main/java` and `src/client/java`. The older `src/com/` tree and root `resources/` remain in the repository for now but are excluded from the build. No player data keys or packet formats were changed in Phase 0.

## Phase 0 validation and limits

- `gradlew.bat build` completed with JDK 25; the jar contains 30 classes, including `SkillScreenV2` and `ServerPlayerSkillDataMixin`.
- The Unix `gradlew` script passed `bash -n` and launched Gradle 9.7.1 under Git Bash on Windows. A native Unix host has not been tested.
- `gradlew.bat runClient` loaded Minecraft 26.3, Fabric API, UI Lib, and Limitless Skills and logged both initialization messages. G, `/skills`, and in-world behavior were not exercised.
- `gradlew.bat runServer` loaded the same mod on the dedicated server classpath and logged both initialization messages. It stopped at the first-run EULA gate, so world startup and persistence were not tested.
- The Windows host emitted Minecraft/OSHI system-information warnings about Windows performance counters during both launches. These did not stop client initialization or the server from reaching the EULA gate.

See [Phase 1 runtime verification](PHASE1_RUNTIME_VERIFICATION.md) for current runtime results and manual test requirements. The UI currently labels level-based nodes as available/unlocked even though point spending is not implemented; this behavior is preserved and should be reviewed separately from the future skill-point design.
