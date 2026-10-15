package com.branden.limitskills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Day-one anti-cheese:
 * - Break/place loops: player-placed blocks are tracked and give no XP when broken.
 * - Spawner farms: diminishing returns per entity type per minute.
 * - AFK: arcane block-use XP is cooldown-gated.
 */
public class AntiCheese {
	// Break/place loop detection: track recent breaks per player.
	// If the same position is broken 3+ times in 90s, it's a loop — no XP.
	private static final Map<UUID, Map<BlockPos, long[]>> BREAKS = new HashMap<>();
	private static final int LOOP_WINDOW_MS = 90_000;
	private static final int LOOP_THRESHOLD = 3;

	// Mob XP diminishing returns: key = uuid + entity type, value = recent kill timestamps
	private static final Map<String, long[]> MOB_WINDOWS = new HashMap<>();
	private static final int WINDOW_MS = 60_000;
	private static final int WINDOW_SLOTS = 12;

	// Generic cooldowns: key = uuid + id
	private static final Map<String, Long> COOLDOWNS = new HashMap<>();

	/** Returns true if this break looks like a place/break loop (no XP). */
	public static boolean isLoopBreak(ServerPlayer player, BlockPos pos) {
		long now = System.currentTimeMillis();
		Map<BlockPos, long[]> map = BREAKS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
		if (map.size() > 512) map.clear();
		long[] times = map.computeIfAbsent(pos.immutable(), k -> new long[LOOP_THRESHOLD]);
		int count = 0;
		int oldest = 0;
		for (int i = 0; i < times.length; i++) {
			if (now - times[i] < LOOP_WINDOW_MS) {
				count++;
			} else if (times[i] < times[oldest]) {
				oldest = i;
			}
		}
		times[oldest] = now;
		return count >= LOOP_THRESHOLD - 1;
	}

	/** Returns true if XP should be awarded (not yet over the farm cap). */
	public static boolean allowMobXp(ServerPlayer player, EntityType<?> type) {
		String key = player.getUUID() + "|" + EntityType.getKey(type);
		long now = System.currentTimeMillis();
		long[] window = MOB_WINDOWS.computeIfAbsent(key, k -> new long[WINDOW_SLOTS]);
		// Count kills in window
		int count = 0;
		int oldest = 0;
		for (int i = 0; i < window.length; i++) {
			if (now - window[i] < WINDOW_MS) {
				count++;
			} else if (window[i] < window[oldest]) {
				oldest = i;
			}
		}
		if (count >= WINDOW_SLOTS) return false; // capped: no XP this kill
		window[oldest] = now;
		return true;
	}

	/** Returns true if the cooldown expired (and resets it). */
	public static boolean checkCooldown(ServerPlayer player, String id, long now, long cooldownMs) {
		String key = player.getUUID() + "|" + id;
		Long last = COOLDOWNS.get(key);
		if (last != null && now - last < cooldownMs) return false;
		COOLDOWNS.put(key, now);
		return true;
	}

	public static void clearPlayer(ServerPlayer player) {
		BREAKS.remove(player.getUUID());
	}
}
