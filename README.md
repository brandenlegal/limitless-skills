# Limitless Skills

A premium RPG progression mod for Minecraft — five skill disciplines with XP tracking, persistent player data, and a polished skill-tree interface.

Part of the [Limitless Labs](https://github.com/brandenlegal) ecosystem.

## Current Version

**v2.0.1** — Minecraft 26.3, Fabric Loader 0.19.5+

## Features

### Five Skill Disciplines
- **⛏ Mining** — Ores, stone, deepslate
- **⚔ Combat** — Mob kills, boss kills
- **🌾 Farming** — Crops, animal breeding
- **❤ Survival** — Damage taken, fishing
- **✦ Arcane** — Enchanting, brewing

### Progression
- Exponential XP curve (`100 × 1.35^level`)
- Level-up notifications with sound
- Per-discipline perks (mining speed, double drops, damage bonuses, etc.)
- Anti-cheat: placed blocks give no XP, rate limiting

### Persistence
- Skill data saves to player NBT via Mixin
- Survives server restarts and disconnects
- Server-authoritative with client sync

### Interface (v2.0)
- Dark charcoal theme with gold accents
- Visual skill-tree canvas with connected nodes
- Node states: Locked (§8) / Available (§6) / Unlocked (§a)
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

This project uses manual `javac` builds (Gradle daemon is blocked in the build environment).
A Fabric Loom scaffold is available on the `loom-pipeline-experiment` branch.

## Branches

- `main` — Stable releases
- `v2.0-redesign` — v2.0 UI + persistence + commands (current development)
- `v1.1.0-persistence-ui` — Persistence milestone
- `loom-pipeline-experiment` — Fabric Loom build scaffold

## License

MIT — see LICENSE file.

## Author

**brandenlegal** — Limitless Labs
