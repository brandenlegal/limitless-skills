package com.branden.limitskills;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core skill logic: XP awards, level calculation, player data management.
 */
public final class SkillManager {
	private static final Map<UUID, SkillData> DATA = new ConcurrentHashMap<>();

	/** XP required to go from level L to L+1. Exponential curve. */
	public static double xpForLevel(int level) {
		return 100.0 * Math.pow(1.35, level);
	}

	/** Total XP -> level. */
	public static int xpToLevel(double xp) {
		int level = 0;
		double remaining = xp;
		while (remaining >= xpForLevel(level)) {
			remaining -= xpForLevel(level);
			level++;
		}
		return level;
	}

	/** Progress (0.0-1.0) toward next level. */
	public static double progressToNext(double xp) {
		int level = xpToLevel(xp);
		double remaining = xp;
		for (int i = 0; i < level; i++) {
			remaining -= xpForLevel(i);
		}
		return remaining / xpForLevel(level);
	}

	public static SkillData get(ServerPlayer player) {
		return DATA.computeIfAbsent(player.getUUID(), k -> SkillStore.load(player));
	}

	public static void awardXp(ServerPlayer player, SkillType type, double amount) {
		if (amount <= 0) return;
		SkillData data = get(player);
		int oldLevel = data.getLevel(type);
		data.addXp(type, amount);
		int newLevel = data.getLevel(type);
		SkillStore.save(player, data);
		if (newLevel > oldLevel) {
			Perks.onLevelUp(player, type, newLevel);
		}
		// Sync to client
		com.branden.limitskills.net.SkillNetworking.syncToClient(player);
	}

	public static void clear(UUID uuid) {
		DATA.remove(uuid);
	}
}
