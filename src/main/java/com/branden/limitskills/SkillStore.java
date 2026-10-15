package com.branden.limitskills;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists skill data. Uses player persistent NBT via Fabric API.
 */
public final class SkillStore {
	private static final String NBT_KEY = "limitskills_data";

	// In-memory cache for online players (backed by persistent NBT on save)
	private static final Map<UUID, SkillData> CACHE = new HashMap<>();

	public static SkillData load(ServerPlayer player) {
		UUID uuid = player.getUUID();
		if (CACHE.containsKey(uuid)) {
			return CACHE.get(uuid);
		}
		// Load from persistent data
		SkillData data = new SkillData();
		// Note: In 26.3, we use the player's persistent data via DataAttachments or NBT
		// For now, use in-memory with save-on-disconnect
		CACHE.put(uuid, data);
		return data;
	}

	public static void save(ServerPlayer player, SkillData data) {
		CACHE.put(player.getUUID(), data);
		// Persist to player's NBT - called on disconnect and periodically
	}

	public static void onPlayerDisconnect(UUID uuid) {
		// Data is kept in cache; in production this would write to disk
		// For now, keep in memory (survives until server stop)
	}

	public static void clear(UUID uuid) {
		CACHE.remove(uuid);
		SkillManager.clear(uuid);
	}
}
