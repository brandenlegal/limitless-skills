package com.branden.limitskills;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Awards skill XP from gameplay events. All anti-cheese lives in AntiCheese.
 */
public class XpHooks {

	// ---- Mining & Farming (block break) ----

	public static void onBlockBreak(ServerPlayer player, BlockPos pos, BlockState state) {
		Block block = state.getBlock();

		// Anti-cheese: break/place loops give nothing
		if (AntiCheese.isLoopBreak(player, pos)) {
			return;
		}

		// Farming: fully-grown crops
		if (block instanceof CropBlock crop && crop.isMaxAge(state)) {
			SkillManager.addXp(player, SkillType.FARMING, 6);
			return;
		}
		if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
			SkillManager.addXp(player, SkillType.FARMING, 8);
			return;
		}
		if (block == Blocks.NETHER_WART) {
			// nether wart max age check via blockstate property would need mapping; flat rate fine
			SkillManager.addXp(player, SkillType.FARMING, 5);
			return;
		}

		// Mining: ores scale by value, stone gives a trickle
		Double oreXp = ORE_XP.get(block);
		if (oreXp != null) {
			SkillManager.addXp(player, SkillType.MINING, oreXp);
			return;
		}
		if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.ANDESITE
				|| block == Blocks.DIORITE || block == Blocks.GRANITE || block == Blocks.TUFF) {
			SkillManager.addXp(player, SkillType.MINING, 1);
		} else if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
			SkillManager.addXp(player, SkillType.MINING, 2);
		} else if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN) {
			SkillManager.addXp(player, SkillType.MINING, 4);
		}
	}

	private static final java.util.Map<Block, Double> ORE_XP = new java.util.HashMap<>();
	static {
		ORE_XP.put(Blocks.COAL_ORE, 8.0);
		ORE_XP.put(Blocks.DEEPSLATE_COAL_ORE, 9.0);
		ORE_XP.put(Blocks.IRON_ORE, 12.0);
		ORE_XP.put(Blocks.DEEPSLATE_IRON_ORE, 13.0);
		ORE_XP.put(Blocks.COPPER_ORE, 6.0);
		ORE_XP.put(Blocks.DEEPSLATE_COPPER_ORE, 7.0);
		ORE_XP.put(Blocks.GOLD_ORE, 20.0);
		ORE_XP.put(Blocks.DEEPSLATE_GOLD_ORE, 22.0);
		ORE_XP.put(Blocks.REDSTONE_ORE, 15.0);
		ORE_XP.put(Blocks.DEEPSLATE_REDSTONE_ORE, 16.0);
		ORE_XP.put(Blocks.LAPIS_ORE, 18.0);
		ORE_XP.put(Blocks.DEEPSLATE_LAPIS_ORE, 19.0);
		ORE_XP.put(Blocks.DIAMOND_ORE, 40.0);
		ORE_XP.put(Blocks.DEEPSLATE_DIAMOND_ORE, 44.0);
		ORE_XP.put(Blocks.EMERALD_ORE, 50.0);
		ORE_XP.put(Blocks.DEEPSLATE_EMERALD_ORE, 55.0);
		ORE_XP.put(Blocks.NETHER_GOLD_ORE, 10.0);
		ORE_XP.put(Blocks.NETHER_QUARTZ_ORE, 10.0);
		ORE_XP.put(Blocks.ANCIENT_DEBRIS, 80.0);
	}

	// ---- Combat ----

	public static void onMobKill(ServerPlayer player, LivingEntity victim) {
		// Spawner-farm cheese: diminishing returns per entity type
		if (!AntiCheese.allowMobXp(player, victim.getType())) return;
		float hp = victim.getMaxHealth();
		double xp = Math.min(hp * 0.8, 40.0);
		// Passive mobs give less
		if (victim.getType().getCategory().isFriendly()) xp *= 0.4;
		SkillManager.addXp(player, SkillType.COMBAT, Math.max(1, xp));
	}

	// ---- Survival ----

	public static void onDamageTaken(ServerPlayer player, float damage) {
		// Surviving hits trains Survival; capped per hit so lava isn't a farm
		SkillManager.addXp(player, SkillType.SURVIVAL, Math.min(damage * 0.5, 10.0));
	}

	public static void onItemUse(ServerPlayer player, ItemStack stack) {
		if (stack.get(net.minecraft.core.component.DataComponents.FOOD) != null) {
			SkillManager.addXp(player, SkillType.SURVIVAL, 4);
			return;
		}
		// Arcane: magical consumables
		if (stack.is(Items.ENDER_PEARL)) {
			SkillManager.addXp(player, SkillType.ARCANE, 6);
		} else if (stack.is(Items.CHORUS_FRUIT)) {
			SkillManager.addXp(player, SkillType.ARCANE, 4);
		} else if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
			SkillManager.addXp(player, SkillType.ARCANE, 8);
		} else if (stack.is(Items.EXPERIENCE_BOTTLE)) {
			SkillManager.addXp(player, SkillType.ARCANE, 3);
		}
	}

	public static void onBlockUse(ServerPlayer player, Level world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		long now = System.currentTimeMillis();
		if (block == Blocks.ENCHANTING_TABLE) {
			if (AntiCheese.checkCooldown(player, "arcane:enchant", now, 30_000)) {
				SkillManager.addXp(player, SkillType.ARCANE, 10);
			}
		} else if (block == Blocks.BREWING_STAND) {
			if (AntiCheese.checkCooldown(player, "arcane:brew", now, 30_000)) {
				SkillManager.addXp(player, SkillType.ARCANE, 8);
			}
		}
	}
}
