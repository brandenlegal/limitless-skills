# Limitless Skills — Known Issues

**Last updated:** October 16, 2026
**Applies to:** v2.0.1 (latest built jar: `limitless-skills-2.0.1-26.3.jar`)
**Branch:** `v2.0-redesign` on `github.com/brandenlegal/limitless-skills`

**Status legend:**
- 🔴 **Reported** — observed in-game by Branden, not yet fixed
- 🟡 **Fixed, unverified** — code changed, but Branden has not confirmed it works in-game
- 🟢 **By design** — intentional limitation, will be addressed in a planned milestone
- ⚪ **Untested** — implemented but never exercised in-game
- 🔧 **Tech debt** — code/process problem, not a gameplay bug

**Honesty rule:** Nothing in this document is marked fixed unless Branden has verified it in-game. "Compiles clean" is not "tested."

---

## 1. `/skills` Client Command Does Nothing In-Game

**Severity:** Medium
**Status:** 🟡 Fixed, unverified (v2.0.1) — originally 🔴 Reported in v2.0

### Description

Branden reported that typing `/skills` in-game does nothing — no GUI appears, no error message, no feedback. The G keybind opens the v2.0 GUI correctly, so the screen itself works; only the command path fails.

### Reproduction steps

1. Install `limitless-skills-2.0.0-26.3.jar` (or earlier) on Fabric 26.3.
2. Join a world.
3. Type `/skills` and press Enter.
4. Observe: nothing happens. (Branden did not report an "unknown command" error, which suggests the command may be registered but its callback never fires — or that the failure is silent.)

### Suspected cause

The client-side command is registered via Fabric API's `ClientCommandRegistrationCallback.EVENT` in `LimitlessSkillsClient.onInitializeClient()`:

```java
ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
    dispatcher.register(
        LiteralArgumentBuilder.<FabricClientCommandSource>literal("skills")
            .executes(context -> {
                openSkillScreen();
                return 1;
            })
    );
    // ... /skilltree <type> similarly
});
```

The registration pattern matches Fabric's documented usage, and the API signature was verified via `javap` against `fabric-command-api-v2-3.1.0`. However, in 26.3 the client command dispatcher lifecycle may differ — the callback may fire before/after the dispatcher the player actually uses is built, or the event may not fire at all in some connection scenarios. This could not be diagnosed definitively without running the game (the sandbox cannot launch Minecraft).

### What was done (v2.0.1)

Rather than debugging blind, a **server-side fallback** was implemented:

- New packet `OpenScreenPayload` (server → client, empty), registered in `SkillNetworking.init()` and handled in `LimitlessSkillsClient` by calling `openSkillScreen()`.
- New server command `/skills` registered via `CommandRegistrationCallback` in `LimitlessSkills.onInitialize()` — executes on the server, sends `OpenScreenPayload` to the player, client opens the GUI.
- The original client-side `/skills` registration was **kept** (it may work in some contexts); the G keybind is untouched.

Server commands are well-tested in Fabric, and the packet flow reuses the same `ServerPlayNetworking`/`ClientPlayNetworking` infrastructure as the working `SkillSyncPayload`.

### What remains unverified

- Whether `/skills` now opens the GUI in-game (the v2.0.1 fallback has not been tested by Branden yet).
- Whether the original client command was actually broken vs. a registration-timing issue.
- Whether `/skilltree <type>` works (same client-command mechanism; likely the same problem).

### Acceptance criteria

- Typing `/skills` in-game opens the v2.0 skills GUI.
- If the client command is confirmed dead, remove it and keep only the server-side command (no dead code).
- `/skilltree <type>` either works or is removed.

---

## 2. Skill Tree Nodes Are Visual Only

**Severity:** High (for gameplay — this is the core missing feature)
**Status:** 🟢 By design (v2.0) — next milestone

### Description

The v2.0 skill tree renders nodes, connections, states (Locked/Available/Unlocked), and the details panel, but clicking a node does nothing functional. There is no way to spend anything, unlock anything, or gain any benefit from the tree. Branden explicitly approved this as display-only for v2.0 ("Do not add fake interactivity. If a node is display-only, make that clear"), with functional allocation deferred to the next milestone.

### Current behavior

- Node states are derived from real data (discipline levels from `ClientSkillCache`), so the *display* is honest.
- Tier 1 nodes show as unlocked at Mining level 5+, tier 2 at level 15+. These thresholds are display logic only — they grant no effects.
- The details panel shows real perks from the `Perks` class, but these perks are passive (always-on based on level), not unlockable.

### What's missing (planned — see ROADMAP Phase 2–3)

- Skill points (not implemented at all — no earning, no balances, no persistence).
- Node unlock mechanics (no click-to-unlock, no server validation).
- Per-node gameplay effects tied to unlocks.

### Acceptance criteria

- Nodes are clickable; clicking selects the node and updates the details panel.
- Eligible nodes can be unlocked by spending skill points, with server-side validation.
- Each unlock grants a real, observable gameplay effect.
- The details panel tells the player exactly what to do next ("Reach Mining Level 5", "1 skill point required", "Prerequisite: Mining Efficiency").

---

## 3. Persistence Not Verified After Restart

**Severity:** High
**Status:** ⚪ Untested

### Description

Player skill data (XP, levels) is saved via a `ServerPlayerSkillDataMixin` that hooks `addAdditionalSaveData` / `readAdditionalSaveData` using the 26.3 `ValueInput`/`ValueOutput` API, with a `SkillStore` cache layer. The code compiles cleanly and the Mixin config (`limitskills.mixins.json`) is registered in `fabric.mod.json`. **However, Branden has never confirmed that data actually survives a server/world restart in-game.**

### Why this matters

If the Mixin injection silently fails (wrong method descriptor, wrong injection point, Mixin not applied), players would lose all progress on every restart — a critical data-loss bug that would only be discovered after hours of play.

### Reproduction / verification steps

1. Fresh world, mine stone until Mining reaches level 2+. Note the exact level.
2. Save and quit to title (singleplayer) or stop the server (dedicated).
3. Reload the world / restart the server.
4. Open the GUI (G key) and confirm the level is still 2+.

### Suspected risks

- The 26.3 `ValueInput`/`ValueOutput` NBT API was used based on `javap` verification, not in-game testing. If `getDoubleOr` / `getIntOr` / `putDouble` / `putInt` behave differently than assumed, saves could silently write nothing.
- `SkillStore.getCached` / `putCached` must correctly bridge the Mixin and the in-memory data. A cache-key mismatch would cause data to save under one key and load from another.

### Acceptance criteria

- Level up → full restart → level persists, confirmed by Branden in both singleplayer and on a dedicated server.

---

## 4. `fabric.mod.json` Version Says `1.0.0`

**Severity:** Low (cosmetic, but confusing)
**Status:** 🔴 Reported (verified in source, Oct 16)

### Description

`src/main/resources/fabric.mod.json` contains `"version": "1.0.0"`, but the latest built release is v2.0.1. The version string is what players and mod managers (Mod Menu, Modrinth) display, so anyone checking the installed mod version sees "1.0.0" while running v2.0.1 code.

### Fix

Change `"version": "1.0.0"` to `"version": "2.0.1"` (one-line edit). Going forward, the version in `fabric.mod.json` must be bumped in the same commit as each release, and jar filenames must never be reused.

### Acceptance criteria

- `fabric.mod.json` version matches the release jar version.
- The version displays correctly in Mod Menu in-game.

---

## 5. Stale `src/com/` Directory (Tech Debt)

**Severity:** Low (no gameplay impact, but a real maintenance hazard)
**Status:** 🔧 Tech debt (verified present, Oct 16)

### Description

The project root contains a stale `src/com/branden/limitskills/...` directory with 12+ Java files duplicating the canonical `src/main/java` and `src/client/java` trees. These are leftovers from the pre-reorganization layout (v1.x era). The canonical source is `src/main/` + `src/client/` — all recent work (v2.0 UI, `/skills` fix, persistence) lives there.

### Why it matters

- Anyone (or any AI agent) inspecting the project may read or edit the wrong files.
- If a build script ever globs `src/**/*.java`, stale files could be compiled alongside canonical ones, causing duplicate-class errors or — worse — silently shadowing newer code.
- It inflates the repository and confuses the GitHub source view.

### Fix

Delete `src/com/` entirely after confirming no unique content exists there (spot-check: the stale `LimitlessSkillsClient.java` predates the server-command fix; the stale `SkillNetworking.java` predates `OpenScreenPayload`). Then verify the build compiles from `src/main` + `src/client` alone.

### Acceptance criteria

- `src/com/` does not exist.
- `ls src/` shows only `main/` and `client/`.
- Clean build succeeds with no references to the deleted directory.

---

## 6. No Working Gradle/Loom Build (Infrastructure)

**Severity:** Medium (blocks standard development workflow)
**Status:** 🔧 Tech debt / environmental limitation

### Description

All jars to date (v1.0.0 through v2.0.1) were built with manual `javac` against downloaded dependency jars (`dl-fabric-api-26.3.jar`, `dl-owo-lib-26.3.jar`). This works but is fragile and non-standard:

- Dependency versions are managed by hand (whatever jar happened to be downloaded).
- No `runClient` / `runServer` dev launchers — every test requires manually assembling a jar and installing it in a real game instance.
- No reproducible build: a fresh checkout cannot produce a jar without the undocumented manual steps.
- Mixin refmaps and access wideners (if ever needed) require Loom.

### Root cause

The sandbox environment blocks the Gradle daemon (IPC startup failure), so Gradle-based builds were impossible during sandbox development. This is environmental, not a project configuration problem — but it means no working `build.gradle` was ever completed and verified.

### What's available

- The `loom-pipeline-experiment` branch contains an earlier Fabric Loom scaffold (with a GitHub Actions workflow), but it was never verified to build — the `.github/` directory push failed via the API, so CI never ran.
- The manual `javac` classpath and compile commands are known and documented in session notes, but not in a checked-in build script.

### Fix (ROADMAP Phase 0)

On the local development machine (where Gradle works), create a standard `build.gradle` + `settings.gradle` with `fabric-loom`, the correct 26.3 mappings, and `modImplementation` entries for Fabric API and ui-lib. Verify `gradlew build` and `gradlew runClient`. See ROADMAP Phase 0 for the full procedure.

### Acceptance criteria

- `gradlew build` succeeds on the local machine and produces a jar that behaves identically to the manual-build jar in-game.
- `gradlew runClient` launches a dev client with the mod loaded.
- A fresh `git clone` can build with one documented command.

---

## 7. GitHub Branch Mess (Process)

**Severity:** Low (no gameplay impact, but a handoff risk)
**Status:** 🔧 Tech debt / process

### Description

The repository (`brandenlegal/limitless-skills`) currently has 4 branches:

| Branch | State |
|---|---|
| `main` | Behind — does not contain v2.0 work |
| `v1.1.0-persistence-ui` | Superseded; messy structure from the earlier Loom experiment |
| `v2.0-redesign` | **Latest** — contains all v2.0 + v2.0.1 source, verified file-by-file on the remote (Oct 16) |
| `loom-pipeline-experiment` | Superseded; messy Loom scaffold, unverified |

### Why it matters

A new developer (or Codex) opening the repo will not know which branch is canonical. `main` being behind is especially dangerous — someone could reasonably start from `main` and lose months of work.

### Additional history

During sandbox development, the MCP/API-based `push_files` tool could not reliably handle nested directory structures (the `.github/` push failed outright; the `src/main` reorganization required multiple batched pushes). All source files were eventually pushed to `v2.0-redesign` and verified individually via `get_file_contents`, but this history means **the remote should be re-verified with a real `git clone` before being treated as authoritative**.

### Fix (ROADMAP Phase 0)

- Decide the canonical branch (recommended: promote `v2.0-redesign` to `main` after review, or cut a clean `develop` from it).
- Tag superseded branches for history, then delete or archive them.
- Verify with a fresh `git clone` that the canonical branch contains the complete working source.

### Acceptance criteria

- One obvious canonical branch; `main` is up to date.
- Fresh clone builds and matches the known-good local source file-for-file.

---

## 8. Multiplayer Sync Untested

**Severity:** Medium
**Status:** ⚪ Untested

### Description

`SkillSyncPayload` (server → client full XP/level sync) is implemented and registered via `PayloadTypeRegistry` / `ClientPlayNetworking` / `ServerPlayNetworking`. Branden confirmed Mining levels up correctly, which implies the sync path works in **singleplayer** (where client and server run in the same process). **Dedicated-server behavior with 2+ players has never been tested.**

### Specific risks

- **Cross-player leakage:** if the sync handler uses a global `ClientSkillCache` without per-player scoping on the server send side, one player's data could overwrite another's. (The send is per-player via `ServerPlayNetworking.send(player, payload)`, which looks correct — but unverified.)
- **Desync on relog:** if sync only fires on certain events (level-up, join), a client that misses a packet could display stale data indefinitely.
- **Unlock race conditions** (future): when skill-point unlocks land in Phase 3, concurrent unlock packets from two players must not interfere.

### Verification steps

1. Run a local dedicated server with the mod installed.
2. Connect two clients.
3. Client A mines stone; confirm Client A's GUI shows increasing Mining XP.
4. Confirm Client B's GUI does **not** show Client A's levels (no leakage).
5. Disconnect and reconnect Client A; confirm its GUI still shows the correct levels after rejoin.

### Acceptance criteria

- Each player's GUI shows only their own data on a dedicated server.
- Reconnecting resyncs correctly.
- No packet errors or exceptions in the server log during the test.

---

## Resolved / Closed

*(No issues have been verified as resolved in-game yet. This section will grow as Branden confirms fixes.)*

---

## How to Report a New Issue

Include: mod version (jar filename), Minecraft version, singleplayer or server, exact steps to reproduce, what you expected, what actually happened, and any relevant log excerpts (`logs/latest.log`). Screenshots of the GUI are helpful for visual issues. Branden is the final authority on whether an issue is actually fixed — in-game verification beats all other evidence.
