package com.branden.limitskills;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * XP award hooks - called from Fabric API event handlers.
 */
public final class XpHooks {
	// Mining XP values
	private static final double ORE_XP = 25.0;
	private static final double STONE_XP = 3.0;
	private static final double DEEPSLATE_XP = 5.0;

	// Combat XP
	private static final double MOB_KILL_XP = 20.0;
	private static final double BOSS_XP = 200.0;

	// Farming XP
	private static final double CROP_XP = 8.0;
	private static final double ANIMAL_BREED_XP = 15.0;

	// Survival XP
	private static final double FISH_XP = 12.0;
	private static final double DAMAGE_TAKEN_XP = 0.5;

	// Arcane XP
	private static final double ENCHANT_XP = 30.0;
	private static final double BREW_XP = 15.0;

	public static void onBlockBreak(ServerPlayer player, ServerLevel level, BlockState state, BlockPos pos, BlockEntity blockEntity) {
		if (!AntiCheese.allowBlockXp(player, pos)) return;

		Block block = state.getBlock();
		String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();

		if (blockId.contains("_ore") || blockId.equals("minecraft:ancient_debris")) {
			SkillManager.awardXp(player, SkillType.MINING, ORE_XP);
		} else if (blockId.contains("deepslate") || blockId.contains("tuff")) {
			SkillManager.awardXp(player, SkillType.MINING, DEEPSLATE_XP);
		} else if (blockId.contains("stone") || blockId.contains("andesite") ||
				   blockId.contains("diorite") || blockId.contains("granite")) {
			SkillManager.awardXp(player, SkillType.MINING, STONE_XP);
		} else if (block instanceof CropBlock crop && crop.isMaxAge(state)) {
			SkillManager.awardXp(player, SkillType.FARMING, CROP_XP);
		} else if (blockId.contains("log") || blockId.contains("stem")) {
			// Woodcutting -> Survival
			SkillManager.awardXp(player, SkillType.SURVIVAL, 5.0);
		}

		int bonusCrops = block instanceof CropBlock crop && crop.isMaxAge(state)
			? Perks.bonusCropYield(SkillManager.get(player).getLevel(SkillType.FARMING)) : 0;
		boolean miningBonus = (blockId.contains("_ore") || blockId.equals("minecraft:ancient_debris")
			|| blockId.contains("stone") || blockId.contains("andesite") || blockId.contains("diorite")
			|| blockId.contains("granite") || blockId.contains("deepslate") || blockId.contains("tuff"))
			&& Perks.rollBonusDrop(SkillManager.get(player).getLevel(SkillType.MINING), level.getRandom());
		int repeats = bonusCrops + (miningBonus ? 1 : 0);
		if (repeats > 0 && !player.isCreative()) {
			for (int i = 0; i < repeats; i++) {
				for (var drop : Block.getDrops(state, level, pos, blockEntity, player, player.getMainHandItem())) {
					Block.popResource(level, pos, drop);
				}
			}
		}
	}

	public static void onMobKill(ServerPlayer player, LivingEntity victim) {
		String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
		if (entityId.contains("ender_dragon") || entityId.contains("wither")) {
			SkillManager.awardXp(player, SkillType.COMBAT, BOSS_XP);
		} else {
			SkillManager.awardXp(player, SkillType.COMBAT, MOB_KILL_XP);
		}
	}

	public static void onDamageTaken(ServerPlayer player, double amount) {
		SkillManager.awardXp(player, SkillType.SURVIVAL, amount * DAMAGE_TAKEN_XP);
	}

	public static void onFishCaught(ServerPlayer player) {
		SkillManager.awardXp(player, SkillType.SURVIVAL, FISH_XP);
	}

	public static void onAnimalBred(ServerPlayer player) {
		SkillManager.awardXp(player, SkillType.FARMING, ANIMAL_BREED_XP);
	}

	public static void onEnchant(ServerPlayer player, int levels) {
		SkillManager.awardXp(player, SkillType.ARCANE, ENCHANT_XP * levels);
	}

	public static void onBrew(ServerPlayer player) {
		SkillManager.awardXp(player, SkillType.ARCANE, BREW_XP);
	}
}
