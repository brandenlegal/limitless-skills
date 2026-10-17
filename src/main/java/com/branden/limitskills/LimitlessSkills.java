package com.branden.limitskills;

import com.branden.limitskills.net.SkillNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
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

		// Server-side /skills command (reliable fallback for client command)
		// Sends a packet to the client to open the GUI
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
				com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal("skills")
					.executes(context -> {
						if (context.getSource().getEntity() instanceof ServerPlayer player) {
							SkillNetworking.openScreen(player);
							return 1;
						}
						return 0;
					})
			);
		});

		// Block break XP
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer sp && world instanceof ServerLevel level) {
				XpHooks.onBlockBreak(sp, level, state, pos, blockEntity);
			}
		});

		// Send persisted skills as soon as the play connection can receive packets.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			SkillNetworking.syncToClient(handler.getPlayer())
		);
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel serverLevel
				&& world.getBlockState(hit.getBlockPos()).is(net.minecraft.world.level.block.Blocks.BREWING_STAND)) {
				BrewingXpOwners.markUsed(serverPlayer, serverLevel, hit.getBlockPos());
			}
			return net.minecraft.world.InteractionResult.PASS;
		});

		// Award XP only after the server confirms a kill or nonfatal damage.
		ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
			if (source.getEntity() instanceof ServerPlayer killer && victim != killer) {
				XpHooks.onMobKill(killer, victim);
			}
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (entity instanceof ServerPlayer player && !blocked && damageTaken > 0) {
				XpHooks.onDamageTaken(player, damageTaken);
			}
		});

		// Player disconnect cleanup
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer player = handler.getPlayer();
			AntiCheese.cleanup(player.getUUID());
			BrewingXpOwners.clear(player.getUUID());
		});

		// Server stop - save all data
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Saving skill data...");
			BrewingXpOwners.clearAll();
		});

		LOGGER.info("Limitless Skills initialized!");
	}
}
