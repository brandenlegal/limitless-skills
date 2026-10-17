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

	// In-memory cache for online players (backed by persistent NBT via Mixin)
	private static final Map<UUID, SkillData> CACHE = new HashMap<>();

	/**
	 * Get cached data (used by Mixin during save).
	 * Returns null if no data cached.
	 */
	public static SkillData getCached(UUID uuid) {
		return CACHE.get(uuid);
	}

	/**
	 * Put data in cache (used by Mixin during load).
	 */
	public static void putCached(UUID uuid, SkillData data) {
		CACHE.put(uuid, data);
	}

	public static SkillData load(ServerPlayer player) {
		UUID uuid = player.getUUID();
		if (CACHE.containsKey(uuid)) {
			return CACHE.get(uuid);
		}
		// Data is loaded via Mixin during player NBT read
		// If not in cache, create fresh (Mixin will populate on next load)
		SkillData data = new SkillData();
		CACHE.put(uuid, data);
		return data;
	}

	public static void save(ServerPlayer player, SkillData data) {
		CACHE.put(player.getUUID(), data);
		// Persist to player's NBT - called on disconnect and periodically
	}

	public static void onPlayerDisconnect(UUID uuid) {
		// Called after PlayerList.remove saves the player's final NBT.
		clear(uuid);
	}

	public static void clear(UUID uuid) {
		CACHE.remove(uuid);
		SkillManager.clear(uuid);
	}
}
