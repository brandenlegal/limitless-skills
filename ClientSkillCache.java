package com.branden.limitskills.client;

import com.branden.limitskills.SkillType;
import com.branden.limitskills.net.SkillSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-side cache of the player's skill data, synced from server.
 */
public class ClientSkillCache {
	private static double[] xp = new double[SkillType.values().length];
	private static int[] levels = new int[SkillType.values().length];

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(SkillSyncPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				xp = payload.xp();
				levels = payload.levels();
			});
		});
	}

	public static double getXp(SkillType type) {
		return xp[type.ordinal()];
	}

	public static int getLevel(SkillType type) {
		return levels[type.ordinal()];
	}
}
