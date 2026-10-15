package com.branden.limitskills;

import net.minecraft.server.level.ServerPlayer;

/**
 * Public API for other mods (e.g. Limitless Enchants) to read skill levels.
 * Arcane tree boosts Limitless Enchants gear.
 */
public class SkillApi {
	/** Returns the player's Arcane level (0 if no data). */
	public static int getArcaneLevel(ServerPlayer player) {
		return SkillManager.getLevel(player, SkillType.ARCANE);
	}

	public static int getLevel(ServerPlayer player, SkillType type) {
		return SkillManager.getLevel(player, type);
	}

	// --- Arcane synergy values for Limitless Enchants ---

	/** Vein Miner bonus cap: +1 block per 2 Arcane levels. */
	public static int veinMinerBonusCap(ServerPlayer player) {
		return getArcaneLevel(player) / 2;
	}

	/** Phoenix cooldown reduction: -2% per Arcane level, max -50%. */
	public static double phoenixCooldownReduction(ServerPlayer player) {
		return Math.min(getArcaneLevel(player) * 0.02, 0.50);
	}

	/** Dodge cap bonus: +0.2% per Arcane level, max +10%. */
	public static double dodgeCapBonus(ServerPlayer player) {
		return Math.min(getArcaneLevel(player) * 0.002, 0.10);
	}
}
