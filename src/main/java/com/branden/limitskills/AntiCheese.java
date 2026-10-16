package com.branden.limitskills;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-exploit: prevents XP farming via block place/break loops.
 * Tracks recently placed blocks to deny XP when they're broken.
 */
public final class AntiCheese {
	// Player UUID -> (BlockPos -> timestamp) of placed blocks
	private static final Map<UUID, Map<BlockPos, Long>> PLACED_BLOCKS = new ConcurrentHashMap<>();
	private static final long PLACED_BLOCK_MEMORY_MS = 60_000; // 1 minute

	// Rate limiting: max XP awards per second per player
	private static final Map<UUID, Long> LAST_XP_TIME = new ConcurrentHashMap<>();
	private static final Map<UUID, Integer> XP_COUNT = new ConcurrentHashMap<>();
	private static final int MAX_XP_PER_SECOND = 20;

	public static void onBlockPlaced(ServerPlayer player, BlockPos pos) {
		PLACED_BLOCKS.computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>())
			.put(pos.immutable(), System.currentTimeMillis());
	}

	/**
	 * Returns true if XP should be awarded for breaking this block.
	 * Returns false if the block was recently placed by the player (anti-cheese).
	 */
	public static boolean allowBlockXp(ServerPlayer player, BlockPos pos) {
		UUID uuid = player.getUUID();
		Map<BlockPos, Long> placed = PLACED_BLOCKS.get(uuid);
		if (placed != null) {
			Long placedTime = placed.get(pos);
			if (placedTime != null) {
				if (System.currentTimeMillis() - placedTime < PLACED_BLOCK_MEMORY_MS) {
					return false; // Recently placed, no XP
				} else {
					placed.remove(pos); // Expired, clean up
				}
			}
		}
		return checkRateLimit(uuid);
	}

	private static boolean checkRateLimit(UUID uuid) {
		long now = System.currentTimeMillis();
		Long lastTime = LAST_XP_TIME.get(uuid);
		if (lastTime == null || now - lastTime > 1000) {
			LAST_XP_TIME.put(uuid, now);
			XP_COUNT.put(uuid, 1);
			return true;
		}
		int count = XP_COUNT.getOrDefault(uuid, 0);
		if (count >= MAX_XP_PER_SECOND) {
			return false; // Rate limited
		}
		XP_COUNT.put(uuid, count + 1);
		return true;
	}

	public static void cleanup(UUID uuid) {
		PLACED_BLOCKS.remove(uuid);
		LAST_XP_TIME.remove(uuid);
		XP_COUNT.remove(uuid);
	}
}
