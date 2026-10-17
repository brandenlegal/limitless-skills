package com.branden.limitskills;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.util.RandomSource;

/**
 * Perk effects applied on level-up and queried for active bonuses.
 * Designed for synergy with Limitless Enchants via SkillApi.
 */
public final class Perks {
	private static final Identifier HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(LimitlessSkills.MOD_ID, "survival_health");
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

	public static boolean rollBonusDrop(int level, RandomSource random) {
		return random.nextDouble() < doubleDropChance(level);
	}

	public static boolean rollCriticalHit(int level, RandomSource random) {
		return random.nextDouble() < critChanceBonus(level);
	}

	public static int discountedEnchantmentLevels(int level, int levels) {
		if (levels <= 0) return levels;
		return Math.max(1, (int) Math.round(levels * (1.0 - enchantCostReduction(level))));
	}

	/** Keep the Survival health bonus derived from the persisted skill level. */
	public static void syncPlayer(ServerPlayer player, SkillData data) {
		AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
		if (health == null) return;
		health.removeModifier(HEALTH_MODIFIER_ID);
		int bonus = bonusHealth(data.getLevel(SkillType.SURVIVAL));
		if (bonus > 0) {
			health.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
		}
	}

	public static void onLevelUp(ServerPlayer player, SkillType type, int newLevel) {
		player.sendSystemMessage(Component.literal("§6§l" + type.displayName + " §elevel up! §7Now level §a" + newLevel));
		player.playSound(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.0f);

		// Milestone rewards every 10 levels
		if (newLevel % 10 == 0) {
			player.sendSystemMessage(Component.literal("§d§lMilestone! §7" + type.displayName + " reached level " + newLevel));
		}
	}
}
