# AGENTS.md — Limitless Skills

Repository instructions for AI coding agents working on Limitless Skills (Limitless Labs, Minecraft 26.3, Fabric). Read this and `docs/CODEX_HANDOFF.md` before making changes.

## Project structure

```
limitless-skills-26.3/
├── src/
│   ├── main/java/com/branden/limitskills/   # Server/common code (canonical)
│   │   ├── LimitlessSkills.java             # Mod initializer, server /skills command
│   │   ├── SkillType.java                   # MINING, COMBAT, FARMING, SURVIVAL, ARCANE
│   │   ├── SkillData.java                   # Per-player XP/levels (pure data, no MC API)
│   │   ├── SkillManager.java                # XP curve 100×1.35^level, awardXp(ServerPlayer,...)
│   │   ├── SkillStore.java                  # Server-side per-UUID cache (Mixin bridge)
│   │   ├── XpHooks.java                     # Block-break / kill XP event logic
│   │   ├── AntiCheese.java                  # Placed-block + rate-limit exploit prevention
│   │   ├── Perks.java                       # Gameplay bonuses derived from levels
│   │   ├── SkillApi.java                    # Public API for Limitless Enchants (keep stable)
│   │   ├── mixin/ServerPlayerSkillDataMixin.java  # NBT persistence via @Inject
│   │   └── net/                             # SkillSyncPayload, OpenScreenPayload, SkillNetworking
│   ├── client/java/com/branden/limitskills/client/  # Client-only code
│   │   ├── LimitlessSkillsClient.java       # G keybind, client commands, packet receivers
│   │   ├── ClientSkillCache.java            # Client mirror of skill data
│   │   ├── SkillScreenV2.java               # Current UI (approved, do not rewrite)
│   │   ├── SkillScreen.java                 # Legacy v1 screen (reference only)
│   │   └── SkillTreePrototypeScreen.java    # Early prototype (reference only)
│   └── main/resources/
│       ├── fabric.mod.json                  # Mod metadata (keep version in sync!)
│       └── limitskills.mixins.json          # Mixin config
├── docs/
│   ├── CODEX_HANDOFF.md                     # Full onboarding (read first)
│   ├── PRD.md / ARCHITECTURE.md / ROADMAP.md / KNOWN_ISSUES.md  # (planned)
├── build.gradle / settings.gradle / gradle.properties  # (to be created — see handoff Task 2)
└── dist/                                    # Build scratch, not source
```

**Canonical source is `src/main/java`, `src/client/java`, `src/main/resources` only.** `src/com/` and root `resources/` are stale duplicates — do not edit them; they are slated for deletion.

## Build

**Target:** standard Fabric Loom/Gradle build (setup is Codex's Task 2 — not yet done).

Until Gradle works:

```bash
# Manual javac reference build (diagnostic use; prefer Gradle once available)
export JH=<jdk-25-home>
CP=<fabric-api jars>:<minecraft 26.3 jars>:<ui-lib jar>
$JH/bin/javac -nowarn -cp "$CP" -d /tmp/build $(find src/main/java src/client/java -name "*.java")
```

After Gradle setup:

```bash
./gradlew build        # compile + remap + package
./gradlew runClient    # dev client with mod loaded
./gradlew runServer    # dedicated dev server
```

- Java 25 is a hard requirement (bytecode target + 26.3 mappings).
- Minecraft target is pinned to **26.3**. Do not change without Branden's approval.
- If `./gradlew` hangs on daemon startup, that's environmental (seen before) — diagnose before assuming `build.gradle` is wrong.

## Code conventions

- **Packages:** `com.branden.limitskills` (common), `com.branden.limitskills.client` (client-only), `...mixin`, `...net`.
- **Client/server separation is strict.** Anything touching rendering, keybinds, or screens lives in `client/` and is annotated `@Environment(EnvType.CLIENT)`. Gameplay logic (XP, unlocks, validation) runs on `ServerPlayer` only.
- **Static utility pattern:** `SkillManager`, `SkillStore`, `Perks`, `XpHooks`, `AntiCheese` are static-method classes with private constructors. Follow the existing style.
- **`SkillData` stays pure:** no Minecraft API imports — it's the serializable data model. Keep it that way so NBT and future datapack work stay clean.
- **Packets** are records implementing `CustomPacketPayload` with a `StreamCodec` and a `Type<...>`. Register clientbound types in `SkillNetworking.init()` via `PayloadTypeRegistry.clientboundPlay()`.
- **Server authority:** unlock/validation logic never trusts the client. Packets from the client are requests; the server decides.
- **`SkillApi` is a stability boundary.** Limitless Enchants calls it at runtime. Don't rename methods or change signatures without a migration plan.

## Testing requirements

1. **Compile check** — every change must compile clean before anything else.
2. **Logic review** — trace the code path (especially Mixin targets and packet flow) before asking for in-game testing.
3. **In-game verification is Branden's job.** He tests via the Modrinth launcher. When handing him a build, say exactly what to test and what to look for.
4. **Never claim "tested" for what only compiled.** Say "compiles clean, awaiting in-game verification" when that's the truth.
5. **Version every jar uniquely** — never reuse a filename (his launcher caches broken jars). Bump `fabric.mod.json` version to match.
6. **Every build ships a brief changelog:** what changed / fixed / added, and what to test.

## Git workflow

- Repo: `https://github.com/brandenlegal/limitless-skills`
- Current dev branch: `v2.0-redesign`. Keep it as the working branch until the Task 1 consolidation decides the canonical branch.
- Experimental work goes on feature branches; `main` stays stable.
- Commit messages are descriptive: what changed and why, not "fix stuff".
- Push whole raw source in proper Fabric structure — **never ZIPs, never jars, never build outputs, never secrets**.
- Update README/docs when features or dependencies change.
- After each milestone, report: commit hash, branch, files changed, tests run, push status. If a push fails, explain exactly what failed.

## What to preserve

These systems work and are the protected baseline — do not regress them while adding features:

- XP tracking for all five disciplines + the `100 × 1.35^level` curve
- Level-up notifications
- `ServerPlayerSkillDataMixin` NBT persistence
- Server → client sync (`SkillSyncPayload` → `ClientSkillCache`)
- G keybind and `/skills` command (both paths: client registration + server fallback)
- The v2.0 UI layout in `SkillScreenV2` (improve incrementally, never rewrite)
- `SkillApi` method signatures

## Common pitfalls (26.3 + Mixins)

- **NBT:** use `ValueInput.getDoubleOr/getIntOr` and `ValueOutput.putDouble/putInt`. Old `CompoundTag` accessors are gone on these paths.
- **`KeyMapping.Category` is a Record** — construct it: `new KeyMapping.Category(Identifier)`. There is no `create()` factory.
- **Client commands** take `FabricClientCommandSource`, not `CommandSourceStack`.
- **ui-lib `AbstractComponent`:** the render-state override is `extractRenderState(GuiGraphicsExtractor, int, int, float, int, int)` — 6 params, verified via javap.
- **Mixins:** never `@Redirect` a method Fabric API also hooks (use `@Inject`); use full method descriptors for overloads; handler param types must match the target exactly; `CallbackInfoReturnable` on non-void targets; keep Mixin statics private and share state via a helper class (`SkillStore`).
- **Dedicated-server safety:** gameplay methods take `ServerPlayer`, never the client player.
- **Item textures** need an `assets/<modid>/items/` definition file or they're invisible with no error.
- **No fake interactivity:** if a UI element doesn't do anything yet, make that visually obvious. Never imply functionality that isn't implemented.

## Product owner

Branden Garay (brandenlegal) — sole tester and final authority on design. Propose significant changes in writing before implementing. Keep messages short and direct. When builds fail repeatedly: stop, research, explain plainly *why*, then build once.
