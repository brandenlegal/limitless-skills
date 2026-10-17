# Phase 1 Runtime Verification

Branch: `codex/phase-1-stability`  
Minecraft: 26.3  
Build: `2.0.3-phase1.0`

## Test matrix

| System | Result | Evidence / limits |
|---|---|---|
| Java 25 clean Gradle build and automated tests | PASS | `clean build --offline`; all 9 tasks completed successfully. |
| G keybind opens v2.0 skill interface | PASS | Opened `SkillScreenV2` in the development client. |
| `/skills` opens the same interface | PASS | Command opened the v2.0 screen in the development client. |
| Mining XP from eligible block breaks | PASS | Breaking eligible blocks awarded Mining XP and crossed the first level threshold. |
| Player-placed block XP prevention | PASS | A player-placed ore did not award Mining XP; a separate eligible test ore awarded XP. |
| Save compatibility across full client process restart | PASS | Mining level 1 and 100 XP were present in player NBT; after relaunching the client, the world loaded with Mining level 1. Existing per-skill NBT keys are unchanged. |
| Integrated-server login data sync | PASS | Persisted level was visible in the reopened skill UI after world reload. |
| Combat kill XP and Survival damage XP | PASS | Runtime tests awarded the expected 20% Combat XP and 1% Survival XP increments. |
| Fishing, breeding, enchanting XP events | NOT TESTED | Hooks compile; actual player actions were not exercised in this pass. |
| Brewing XP event | NOT TESTED | Brewing stand attribution and brew completion hooks are registered; no complete brew was performed. |
| Passive perk formulas | PASS | Unit tests cover documented caps/steps, discount rounding, and minimum enchant cost. |
| Passive perk in-world effects | NOT TESTED | Mining speed, bonus drops, crop yield, damage/critical, health, enchant cost, and potion duration hooks compile but were not individually exercised in-game. |
| Dedicated server initialization | PASS | Server loaded Fabric, UI Lib, and Limitless Skills and reached world startup without client-only class loading failure. |
| Dedicated server client join / login sync | NOT TESTED | The test client was rejected because the local server whitelist was enabled. The Gradle-launched server had closed stdin, preventing a safe console command to disable it during that run. |
| Multiple simultaneous players remain isolated | PASS / PARTIAL | Automated cache isolation test passes. A two-client multiplayer session was not run. |
| Mixin startup | PASS / PARTIAL | Client and server reached initialization without a mixin application failure or crash. Perk injections have not each been behavior-tested. |
| v2.0 UI layout | PASS / LIMITATION | Screen opens; at 856×512 the tree/details layout clips. Re-test at supported resolutions before UI polish. |

## Logs and environment notes

- The Windows host reports OSHI/Windows performance-counter warnings and process-info errors from Minecraft during startup. They did not prevent client or dedicated-server initialization.
- The offline development client reports authentication/Realms 401 errors because the launch uses a synthetic username. These are unrelated to Limitless Skills initialization.
- Client and server shared Loom's `run/` directory, causing log rotation contention while both were running. Run them separately for cleaner logs in future verification.
- The local development server EULA file was set to `eula=true` with user authorization. The local server uses offline mode and binds to localhost for testing only.
- The global shell selected Java 21 for one attempted build; Loom requires Java 25. Builds pass when `JAVA_HOME` and `PATH` point to the installed Java 25 runtime as documented in `BUILD.md`.

## Manual follow-up

1. Start a dedicated server with whitelist disabled for the test profile, connect the development client, and verify login-time sync plus a server-awarded XP update.
2. Exercise fishing, animal breeding, enchanting, and brewing to confirm event attribution and XP values.
3. Give test characters sufficient levels through a temporary test fixture or controlled save and verify each passive perk in-world.
4. Connect two clients and confirm their XP, levels, and health modifiers remain separate.
5. Recheck the interface at common 16:9 and 16:10 resolutions; record the minimum supported resolution.
