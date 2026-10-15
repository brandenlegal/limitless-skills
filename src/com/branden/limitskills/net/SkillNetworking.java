package com.branden.limitskills.net;

import com.branden.limitskills.SkillData;
import com.branden.limitskills.SkillManager;
import com.branden.limitskills.SkillType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers payloads and handles server-side sending.
 */
public class SkillNetworking {
	public static void init() {
		PayloadTypeRegistry.playS2C().register(SkillSyncPayload.TYPE, SkillSyncPayload.CODEC);
	}

	public static void syncTo(ServerPlayer player) {
		SkillData data = SkillManager.get(player);
		double[] xp = new double[SkillType.values().length];
		int[] levels = new int[SkillType.values().length];
		for (SkillType t : SkillType.values()) {
			xp[t.ordinal()] = data.getXp(t);
			levels[t.ordinal()] = data.getLevel(t);
		}
		ServerPlayNetworking.send(player, new SkillSyncPayload(xp, levels));
	}
}
