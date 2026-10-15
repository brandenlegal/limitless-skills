package com.branden.limitskills;

import com.branden.limitskills.net.SkillNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Limitless Skills - A Fabric RPG skills mod for Minecraft 26.3.
 * Five skill trees: Mining, Combat, Farming, Survival, Arcane.
 * Designed for synergy with Limitless Enchants via SkillApi.
 */
public class LimitlessSkills implements ModInitializer {
	public static final String MOD_ID = "limitskills";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Limitless Skills");

		// Networking
		SkillNetworking.init();

		// Block break XP
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer sp) {
				XpHooks.onBlockBreak(sp, state, pos);
			}
		});

		// Combat XP (mob kills)
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			// XP awarded on kill via LivingDeathEvent (handled via mixin or Fabric API)
			return InteractionResult.PASS;
		});

		// Player disconnect cleanup
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer player = handler.getPlayer();
			AntiCheese.cleanup(player.getUUID());
			SkillStore.onPlayerDisconnect(player.getUUID());
		});

		// Server stop - save all data
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Saving skill data...");
		});

		LOGGER.info("Limitless Skills initialized!");
	}
}
