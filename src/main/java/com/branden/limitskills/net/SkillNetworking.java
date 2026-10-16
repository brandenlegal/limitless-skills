package com.branden.limitskills.net;

import com.branden.limitskills.SkillManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Network setup for skill synchronization.
 */
public final class SkillNetworking {
	public static void init() {
		PayloadTypeRegistry.clientboundPlay().register(SkillSyncPayload.TYPE, SkillSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(OpenScreenPayload.TYPE, OpenScreenPayload.CODEC);
	}

	public static void syncToClient(ServerPlayer player) {
		var data = SkillManager.get(player);
		var payload = new SkillSyncPayload(data.getAllXp(), data.getAllLevels());
		ServerPlayNetworking.send(player, payload);
	}

	public static void openScreen(ServerPlayer player) {
		ServerPlayNetworking.send(player, new OpenScreenPayload());
	}
}
