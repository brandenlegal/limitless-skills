package com.branden.limitskills;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.branden.limitskills.net.SkillNetworking;

public class LimitlessSkills implements ModInitializer {
	public static final String MOD_ID = "limitskills";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Limitless Skills initializing");

		SkillNetworking.init();

		// Mining + Farming XP (block break)
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!(player instanceof ServerPlayer sp)) return;
			XpHooks.onBlockBreak(sp, pos, state);
		});

		// Combat XP (entity death)
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (damageSource.getEntity() instanceof ServerPlayer sp) {
				XpHooks.onMobKill(sp, entity);
			}
		});

		// Survival XP (taking damage and living)
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (entity instanceof ServerPlayer sp && damageTaken > 0 && sp.isAlive()) {
				XpHooks.onDamageTaken(sp, damageTaken);
			}
		});

		// Survival (eating) + Arcane (pearls, potions, chorus) XP
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (player instanceof ServerPlayer sp) {
				XpHooks.onItemUse(sp, player.getItemInHand(hand));
			}
			return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
		});

		// Arcane XP (enchanting table, brewing stand)
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player instanceof ServerPlayer sp) {
				XpHooks.onBlockUse(sp, world, hitResult.getBlockPos());
			}
			return InteractionResult.PASS;
		});

		// Load skill data for persistence (world-save JSON)
		SkillStore.init();

		LOGGER.info("Limitless Skills initialized");
	}
}
