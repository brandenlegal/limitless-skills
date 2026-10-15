package com.branden.limitskills;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side skill state. Keyed by player UUID, persisted via PlayerDataMixin.
 */
public class SkillManager {
	private static final Map<UUID, SkillData> DATA = new ConcurrentHashMap<>();

	public static final int MAX_LEVEL = 50;

	public static SkillData get(ServerPlayer player) {
		return DATA.computeIfAbsent(player.getUUID(), k -> new SkillData());
	}

	public static void load(ServerPlayer player, CompoundTag tag) {
		DATA.put(player.getUUID(), SkillData.fromNbt(tag));
	}

	public static CompoundTag save(ServerPlayer player) {
		SkillData data = DATA.get(player.getUUID());
		return data != null ? data.toNbt() : new CompoundTag();
	}

	public static void unload(ServerPlayer player) {
		// Keep in memory; save() is called on disconnect via mixin.
	}

	/**
	 * Award XP. Handles level-ups and returns levels gained.
	 */
	public static int addXp(ServerPlayer player, SkillType type, double amount) {
		if (amount <= 0) return 0;
		SkillData data = get(player);
		int level = data.getLevel(type);
		if (level >= MAX_LEVEL) return 0;

		double xp = data.getXp(type) + amount;
		int gained = 0;
		while (level < MAX_LEVEL) {
			double needed = SkillData.xpForLevel(level);
			if (xp < needed) break;
			xp -= needed;
			level++;
			gained++;
		}
		data.setXp(type, xp);
		data.setLevel(type, level);

		if (gained > 0) {
			Perks.onLevelUp(player, type, level, gained);
			com.branden.limitskills.net.SkillNetworking.syncTo(player);
		}
		return gained;
	}

	public static int getLevel(ServerPlayer player, SkillType type) {
		SkillData data = DATA.get(player.getUUID());
		return data != null ? data.getLevel(type) : 0;
	}
}
