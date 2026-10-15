package com.branden.limitskills;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Public API for other mods (like Limitless Enchants) to interact with Limitless Skills.
 * This enables cross-mod synergy: enchantments can check skill levels, award XP, etc.
 */
public final class SkillApi {
	private SkillApi() {}

	/**
	 * Get a player's skill level. Safe to call from any mod.
	 * Returns 0 if the player has no data or skills aren't loaded.
	 */
	public static int getLevel(Player player, SkillType type) {
		if (player instanceof ServerPlayer sp) {
			return SkillManager.get(sp).getLevel(type);
		}
		// Client side - use cache
		return com.branden.limitskills.client.ClientSkillCache.getLevel(type);
	}

	/**
	 * Get a player's skill XP.
	 */
	public static double getXp(Player player, SkillType type) {
		if (player instanceof ServerPlayer sp) {
			return SkillManager.get(sp).getXp(type);
		}
		return com.branden.limitskills.client.ClientSkillCache.getXp(type);
	}

	/**
	 * Award XP to a player (server side only).
	 * Used by Limitless Enchants to grant Arcane XP for custom enchantment actions.
	 */
	public static void awardXp(ServerPlayer player, SkillType type, double amount) {
		SkillManager.awardXp(player, type, amount);
	}

	/**
	 * Check if Limitless Skills is loaded and active.
	 */
	public static boolean isAvailable() {
		try {
			Class.forName("com.branden.limitskills.LimitlessSkills");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Get total combined level across all skills.
	 */
	public static int getTotalLevel(Player player) {
		int total = 0;
		for (SkillType t : SkillType.values()) {
			total += getLevel(player, t);
		}
		return total;
	}
}
