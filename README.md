# Limitless Skills

A Fabric RPG progression mod for Minecraft 26.3 with five defined disciplines and a v2.0 skill screen. Gameplay and persistence are still being verified.

Part of the [Limitless Labs](https://github.com/brandenlegal) ecosystem.

## Current Version

Latest previously shipped build: **v2.0.1**. Current development build: **2.0.3-phase1.0** on `codex/phase-1-stability` (not a release).

## Features

### Five Skill Disciplines
Server events award Mining XP for eligible blocks, Farming XP for mature crops and breeding, Survival XP for logs, fishing, and damage, Combat XP for kills, and Arcane XP for enchanting and brewing. Runtime verification varies by system; see [Phase 1 verification](docs/PHASE1_RUNTIME_VERIFICATION.md).

### Progression
- Exponential XP curve (`100 × 1.35^level`)
- Level-up notifications with sound
- Documented passive perk effects are wired to server-side gameplay hooks; formula unit tests pass, while individual in-world effects still need manual verification.
- Block-break XP is rate limited. Successful player block placements are tracked for the anti-farming guard.

### Persistence
- Skill data saves to player NBT via Mixin
- Player NBT save/load is verified across a full game process restart in the Phase 1 test world.
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
- `codex/phase-1-stability` — runtime verification and core stability
- `v1.1.0-persistence-ui` — Persistence milestone
- `loom-pipeline-experiment` — Fabric Loom build scaffold

## License

The mod metadata declares MIT. A LICENSE file is not yet checked in.

## Author

**brandenlegal** — Limitless Labs
