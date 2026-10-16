package com.branden.limitskills;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Perk effects applied on level-up and queried for active bonuses.
 * Designed for synergy with Limitless Enchants via SkillApi.
 */
public final class Perks {
	/** Mining: +2% mining speed per level (applied via attribute modifier). */
	public static double miningSpeedBonus(int level) {
		return level * 0.02;
	}

	/** Mining: double-drop chance, caps at 25%. */
	public static double doubleDropChance(int level) {
		return Math.min(0.25, level * 0.01);
	}

	/** Combat: +1.5% damage per level. */
	public static double damageBonus(int level) {
		return level * 0.015;
	}

	/** Combat: crit chance bonus, caps at 20%. */
	public static double critChanceBonus(int level) {
		return Math.min(0.20, level * 0.008);
	}

	/** Farming: +1 crop yield every 10 levels. */
	public static int bonusCropYield(int level) {
		return level / 10;
	}

	/** Survival: damage reduction, caps at 30%. */
	public static double damageReduction(int level) {
		return Math.min(0.30, level * 0.012);
	}

	/** Survival: +1 max health every 8 levels (2 HP = 1 heart). */
	public static int bonusHealth(int level) {
		return (level / 8) * 2;
	}

	/** Arcane: XP cost reduction for enchanting, caps at 50%. */
	public static double enchantCostReduction(int level) {
		return Math.min(0.50, level * 0.02);
	}

	/** Arcane: potion duration bonus. */
	public static double potionDurationBonus(int level) {
		return level * 0.02;
	}

	public static void onLevelUp(ServerPlayer player, SkillType type, int newLevel) {
		player.sendSystemMessage(Component.literal("§6§l" + type.displayName + " §elevel up! §7Now level §a" + newLevel));
		player.playSound(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.0f);

		// Milestone rewards every 10 levels
		if (newLevel % 10 == 0) {
			player.sendSystemMessage(Component.literal("§d§lMilestone! §7" + type.displayName + " reached level " + newLevel));
			// Grant a reward - could integrate with Limitless Enchants here
		}
	}
}
