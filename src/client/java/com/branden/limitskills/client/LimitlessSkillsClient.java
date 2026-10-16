package com.branden.limitskills.client;

import com.branden.limitskills.net.OpenScreenPayload;
import com.branden.limitskills.net.SkillSyncPayload;
import com.branden.limitskills.SkillApi;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Client-side initialization: keybind, network handlers.
 */
@Environment(EnvType.CLIENT)
public class LimitlessSkillsClient implements ClientModInitializer {

	public static KeyMapping openSkillsKey;
	private static com.branden.limitskills.SkillType pendingPrototype;
	private static int pendingPrototypeTicks;

	@Override
	public void onInitializeClient() {
		SkillApi.setClientDataAccess(ClientSkillCache::getLevel, ClientSkillCache::getXp);
		// Register G key to open skills screen
		openSkillsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.limitskills.open_skills",
			InputConstants.Type.KEYBOARD,
			InputConstants.KEY_G,
			new KeyMapping.Category(net.minecraft.resources.Identifier.fromNamespaceAndPath("limitskills", "skills"))
		));

		// Check key press each tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSkillsKey.consumeClick()) {
				openSkillScreen();
			}
			// Brigadier runs while chat is still closing. Open the prototype on the
			// next client tick so chat cannot replace the new screen.
			if (pendingPrototype != null && --pendingPrototypeTicks <= 0) {
				var type = pendingPrototype;
				pendingPrototype = null;
				openSkillTreePrototype(type);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientSkillCache.clear();
			pendingPrototype = null;
		});

		// The server owns /skills and sends the open-screen packet. Keep only the
		// local prototype command here so /skills reaches the server.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			// /skilltree <type> opens the prototype
			dispatcher.register(
				com.mojang.brigadier.builder.LiteralArgumentBuilder.<FabricClientCommandSource>literal("skilltree")
					.then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
						.executes(context -> {
							String typeName = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "type");
							try {
								com.branden.limitskills.SkillType type = com.branden.limitskills.SkillType.valueOf(typeName.toUpperCase());
								pendingPrototype = type;
								pendingPrototypeTicks = 2;
							} catch (IllegalArgumentException e) {
								context.getSource().sendFeedback(net.minecraft.network.chat.Component.literal("Unknown skill type: " + typeName));
							}
							return 1;
						})
					)
			);
		});

		// Handle skill sync from server
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
			SkillSyncPayload.TYPE,
			(payload, context) -> {
				context.client().execute(() -> {
					ClientSkillCache.update(payload.xp(), payload.levels());
				});
			}
		);

		// Handle open-screen request from server (via /skills command)
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
			OpenScreenPayload.TYPE,
			(payload, context) -> {
				context.client().execute(() -> {
					openSkillScreen();
				});
			}
		);
	}

	/**
	 * Open the skill screen. Called from keybind or command.
	 * Uses the v2.0 premium interface.
	 */
	public static void openSkillScreen() {
		Minecraft.getInstance().setScreenAndShow(new SkillScreenV2());
	}

	/**
	 * Open the skill-tree prototype screen.
	 */
	public static void openSkillTreePrototype(com.branden.limitskills.SkillType type) {
		Minecraft.getInstance().setScreenAndShow(new SkillTreePrototypeScreen(type));
	}
}
