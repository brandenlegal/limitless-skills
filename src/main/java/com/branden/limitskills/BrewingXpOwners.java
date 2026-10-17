package com.branden.limitskills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks the most recent player to use a brewing stand for server-side brew XP attribution. */
public final class BrewingXpOwners {
	private static final Map<GlobalPos, UUID> OWNERS = new HashMap<>();

	private BrewingXpOwners() {}

	public static void markUsed(ServerPlayer player, ServerLevel level, BlockPos pos) {
		OWNERS.put(GlobalPos.of(level.dimension(), pos), player.getUUID());
	}

	public static void onBrew(ServerLevel level, BlockPos pos) {
		UUID ownerId = OWNERS.get(GlobalPos.of(level.dimension(), pos));
		if (ownerId == null) return;
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
		if (owner != null) XpHooks.onBrew(owner);
	}

	public static void clear(UUID playerId) {
		OWNERS.values().removeIf(playerId::equals);
	}

	public static void clearAll() {
		OWNERS.clear();
	}
}
