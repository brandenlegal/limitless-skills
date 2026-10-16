# Limitless Skills — File Inventory (v2.0.1)

Generated Oct 16, 2026. Canonical source: `~/workspace/limitless-skills-26.3/`

## Compiled JARs (in `~/workspace/your_files/limitless-skills-26.3/`)

| Filename | Size | Date | Contents |
|----------|------|------|----------|
| limitless-skills-1.0.0-26.3.jar | 22K | Oct 15 | Phase 1: XP tracking, 5 disciplines, SkillApi |
| limitless-skills-1.1.0-26.3.jar | 31K | Oct 16 | + Persistence (Mixin), G keybind, /skills, /skilltree |
| limitless-skills-2.0.0-26.3.jar | 45K | Oct 16 | + v2.0 premium UI (SkillScreenV2) |
| limitless-skills-2.0.1-26.3.jar | 47K | Oct 16 | + /skills server fallback (OpenScreenPayload) |

**Latest working build:** `limitless-skills-2.0.1-26.3.jar` (30 classes)

## Source Files (canonical: `src/main/java`, `src/client/java`, `src/main/resources`)

### Server/Common (`src/main/java/com/branden/limitskills/`)
| File | Lines | Purpose |
|------|-------|---------|
| LimitlessSkills.java | 74 | ModInitializer. Networking, XP hooks, server /skills command |
| SkillType.java | 29 | Enum: MINING, COMBAT, FARMING, SURVIVAL, ARCANE |
| SkillData.java | 60 | Per-player XP/levels arrays |
| SkillManager.java | 62 | XP curve (100×1.35^L), awardXp, level-up |
| SkillStore.java | 62 | Persistence cache, Mixin integration |
| XpHooks.java | 84 | Block break / mob kill / enchant / brew XP awards |
| Perks.java | 68 | Level-scaled perk calculations + level-up notification |
| SkillApi.java | 65 | Public API for Limitless Enchants |
| AntiCheese.java | 70 | Placed-block tracking, rate limiting |
| mixin/ServerPlayerSkillDataMixin.java | 53 | NBT save/load via 26.3 ValueInput/ValueOutput |
| net/SkillSyncPayload.java | 37 | S→C: full XP/level sync |
| net/OpenScreenPayload.java | 26 | S→C: open GUI request |
| net/SkillNetworking.java | 26 | Payload registration, send helpers |

### Client (`src/client/java/com/branden/limitskills/client/`)
| File | Lines | Purpose |
|------|-------|---------|
| LimitlessSkillsClient.java | 103 | Keybind, commands, packet receivers |
| ClientSkillCache.java | 30 | Client-side XP/level copy |
| SkillScreenV2.java | 551 | v2.0 premium UI (current) |
| SkillScreen.java | 105 | v1.0 basic UI (unused, kept) |
| SkillTreePrototypeScreen.java | 203 | Prototype (used by /skilltree) |

### Resources (`src/main/resources/`)
| File | Purpose |
|------|---------|
| fabric.mod.json | Mod metadata (NOTE: version says 1.0.0, should be 2.0.1) |
| limitskills.mixins.json | Mixin config |

## Documentation (new, Oct 16)

| File | Words | Purpose |
|------|-------|---------|
| docs/PRD.md | 4,024 | Product requirements, game design, skill system |
| docs/ARCHITECTURE.md | 4,364 | Technical architecture, class reference |
| docs/ROADMAP.md | 4,244 | 8-phase development roadmap |
| docs/KNOWN_ISSUES.md | 2,157 | 8 known bugs/limitations |
| docs/CODEX_HANDOFF.md | 3,124 | Instructions for Codex |
| AGENTS.md | 952 | Repository instructions |
| docs/FILE_INVENTORY.md | — | This file |

**Total documentation:** ~18,865 words

## Stale / To Clean Up

- `src/com/branden/limitskills/` — old duplicate source (12-16 files). DELETE after verifying uniqueness.
- `src/main/java` version in fabric.mod.json — says "1.0.0", should be "2.0.1".

## GitHub Status

- Repo: https://github.com/brandenlegal/limitless-skills
- Branch with latest code: `v2.0-redesign` (all source files verified on remote Oct 16)
- Other branches: `main` (behind), `v1.1.0-persistence-ui`, `loom-pipeline-experiment` (messy)
- The 6 new docs + inventory are LOCAL ONLY (not yet pushed to GitHub)
