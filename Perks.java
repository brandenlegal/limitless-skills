package com.branden.limitskills;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

/**
 * Applies per-level stat bumps and tracks perk tiers.
 * Perk choices (1-of-2 every 5 levels) are picked in the skill UI.
 */
public class Perks {
	private static ResourceLocation id(String path) {
		return ResourceLocation.parse("limitskills:" + path);
	}

	public static void onLevelUp(ServerPlayer player, SkillType type, int newLevel, int gained) {
		player.sendSystemMessage(Component.literal("§6§lLEVEL UP! §e" + type.displayName + " §7→ §e" + newLevel));
		applyStatBump(player, type, newLevel);

		// Perk tier every 5 levels
		if (newLevel % 5 == 0) {
			int tier = newLevel / 5;
			player.sendSystemMessage(Component.literal("§d§lPERK UNLOCKED! §7Open the Skills screen (G) to choose your §dTier " + tier + " §7perk for §d" + type.displayName));
		}
		// Signature actives
		if (newLevel == 25) {
			player.sendSystemMessage(Component.literal("§b§lSIGNATURE ABILITY! §7" + signatureName(type, 25) + " unlocked — check your Skills screen"));
		} else if (newLevel == 50) {
			player.sendSystemMessage(Component.literal("§b§lULTIMATE ABILITY! §7" + signatureName(type, 50) + " unlocked — check your Skills screen"));
		}
	}

	public static String signatureName(SkillType type, int level) {
		return switch (type) {
			case MINING -> level == 25 ? "Seismic Sense" : "Worldsplitter";
			case COMBAT -> level == 25 ? "Bloodlust" : "Executioner";
			case FARMING -> level == 25 ? "Green Thumb" : "Harvest Moon";
			case SURVIVAL -> level == 25 ? "Iron Gut" : "Undying";
			case ARCANE -> level == 25 ? "Mana Surge" : "Archmage";
		};
	}

	/**
	 * Small stat bump per level, applied as attribute modifiers.
	 */
	public static void applyStatBump(ServerPlayer player, SkillType type, int level) {
		switch (type) {
			case MINING -> {
				// +2% block break speed per level (applied via custom modifier keyed off level)
				// Actual break-speed hook reads SkillManager.getLevel directly.
			}
			case COMBAT -> {
				setModifier(player, Attributes.ATTACK_DAMAGE, "limitskills:combat_dmg",
					0.04 * level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			}
			case FARMING -> {
				// Crop growth / yield handled in harvest hook via level lookup.
			}
			case SURVIVAL -> {
				setModifier(player, Attributes.MAX_HEALTH, "limitskills:survival_hp",
					0.4 * level, AttributeModifier.Operation.ADD_VALUE);
			}
			case ARCANE -> {
				// XP/enchant boosts handled in hooks via level lookup.
			}
		}
	}

	private static void setModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
			String key, double amount, AttributeModifier.Operation op) {
		AttributeInstance inst = player.getAttribute(attr);
		if (inst == null) return;
		ResourceLocation rl = ResourceLocation.parse("limitskills:" + key.substring(key.indexOf(':') + 1));
		inst.removeModifier(rl);
		inst.addPermanentModifier(new AttributeModifier(rl, amount, op));
	}

	/** Re-apply all stat bumps (e.g. on login / respawn). */
	public static void reapplyAll(ServerPlayer player) {
		net.minecraft.world.entity.ai.attributes.AttributeMap map = player.getAttributes();
		for (SkillType t : SkillType.values()) {
			int level = SkillManager.getLevel(player, t);
			if (level > 0) applyStatBump(player, t, level);
		}
	}
}
