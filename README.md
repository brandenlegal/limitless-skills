# Limitless Skills

A Fabric RPG progression mod for Minecraft 26.3 with five defined disciplines and a v2.0 skill screen. Gameplay and persistence are still being verified.

Part of the [Limitless Labs](https://github.com/brandenlegal) ecosystem.

## Current Version

Latest previously shipped build: **v2.0.1**. Current development build: **2.0.2-dev.1** on `codex/phase-0-build` (not a release).

## Features

### Five Skill Disciplines
Mining, Combat, Farming, Survival, and Arcane are defined and displayed. The current event wiring awards Mining XP for blocks, Farming XP for mature crops, and Survival XP for logs. The other XP source methods are not yet connected to game events.

### Progression
- Exponential XP curve (`100 × 1.35^level`)
- Level-up notifications with sound
- Per-discipline perk formulas are displayed in the UI; gameplay effects are not yet connected.
- Block-break XP is rate limited. Placed-block tracking is not yet connected to a placement event.

### Persistence
- Skill data saves to player NBT via Mixin
- Player NBT save/load Mixin is present; persistence across a full restart still needs an in-game test.
- Server-authoritative with client sync

### Interface (v2.0)
- Dark charcoal theme with gold accents
- Visual skill-tree canvas with connected nodes
- Node states currently reflect level thresholds only; there is no purchase system yet.
- Details panel: level, progress bar, perks, next unlock
- Discipline tabs for switching trees

## Usage

- **G** — Open skills screen
- **`/skills`** — Open skills screen (server command)
- **`/skilltree <type>`** — Open prototype tree (e.g., `/skilltree mining`)

## Dependencies

- Minecraft 26.3
- Fabric Loader ≥0.19.5
- Fabric API ≥0.155.2
- ui-lib ≥22.1.0 (UI framework)
- Java 25

## Installation

1. Install Fabric Loader for Minecraft 26.3
2. Drop `limitless-skills-*.jar` into your `mods` folder
3. Install Fabric API and ui-lib

## For Developers

### Project Structure
```
src/
├── main/java/com/branden/limitskills/
│   ├── LimitlessSkills.java      # Mod init, server command
│   ├── SkillType.java            # 5 disciplines
│   ├── SkillData.java            # Per-player XP/levels
│   ├── SkillManager.java         # XP curve, award logic
│   ├── SkillStore.java           # Persistence cache
│   ├── XpHooks.java              # Event handlers
│   ├── Perks.java                # Perk calculations
│   ├── SkillApi.java             # Public API (Limitless Labs)
│   ├── AntiCheese.java           # Exploit prevention
│   ├── mixin/                    # ServerPlayer NBT persistence
│   └── net/                      # Packets (SkillSync, OpenScreen)
├── client/java/com/branden/limitskills/client/
│   ├── LimitlessSkillsClient.java # Keybind, commands, packets
│   ├── SkillScreenV2.java         # v2.0 premium UI
│   ├── ClientSkillCache.java       # Client data cache
│   └── ...
└── main/resources/
    ├── fabric.mod.json
    └── limitskills.mixins.json
```

### SkillApi (Limitless Labs Ecosystem)

Other Limitless Labs mods can query skill data:

```java
// Check if Skills is available
if (SkillApi.isAvailable()) {
    int miningLevel = SkillApi.getLevel(player, SkillType.MINING);
    SkillApi.awardXp(player, SkillType.COMBAT, 50.0);
}
```

See `LIMITLESS_LABS_ECOSYSTEM_STANDARD.md` for integration conventions.

### Building

Use the checked-in Gradle wrapper and Java 25. See [docs/BUILD.md](docs/BUILD.md) for pinned dependencies, commands, and validation status.

## Branches

- `main` — Stable releases
- `v2.0-redesign` — v2.0 UI + persistence + commands (source baseline)
- `codex/phase-0-build` — reproducible Loom build work
- `v1.1.0-persistence-ui` — Persistence milestone
- `loom-pipeline-experiment` — Fabric Loom build scaffold

## License

The mod metadata declares MIT. A LICENSE file is not yet checked in.

## Author

**brandenlegal** — Limitless Labs
