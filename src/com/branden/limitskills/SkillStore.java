package com.branden.limitskills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists skill data to <world>/limitskills.json (manual JSON, no Gson).
 * No mixins needed.
 */
public class SkillStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(LimitlessSkills.MOD_ID);
	private static MinecraftServer server;
	private static Path saveFile;
	// UUID -> serialized skill data string
	private static final Map<UUID, String> PENDING = new HashMap<>();

	public static void init() {
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			server = s;
			saveFile = s.getWorldPath(LevelResource.ROOT).resolve("limitskills.json");
			loadAll();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> saveAll());

		ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> {
			ServerPlayer player = handler.getPlayer();
			String data = PENDING.remove(player.getUUID());
			if (data != null) {
				SkillManager.load(player, deserialize(data));
			}
			Perks.reapplyAll(player);
			com.branden.limitskills.net.SkillNetworking.syncTo(player);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> {
			ServerPlayer player = handler.getPlayer();
			PENDING.put(player.getUUID(), serialize(SkillManager.save(player)));
			AntiCheese.clearPlayer(player);
			saveAll();
		});
	}

	private static void loadAll() {
		PENDING.clear();
		if (saveFile == null || !Files.exists(saveFile)) return;
		try {
			for (String line : Files.readAllLines(saveFile)) {
				int eq = line.indexOf('=');
				if (eq < 0) continue;
				try {
					PENDING.put(UUID.fromString(line.substring(0, eq)), line.substring(eq + 1));
				} catch (IllegalArgumentException ignored) {}
			}
			LOGGER.info("Loaded Limitless Skills data for {} players", PENDING.size());
		} catch (IOException e) {
			LOGGER.error("Failed to load limitskills data", e);
		}
	}

	private static void saveAll() {
		if (saveFile == null) return;
		try {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<UUID, String> e : PENDING.entrySet()) {
				sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
			}
			if (server != null) {
				for (ServerPlayer p : server.getPlayerList().getPlayers()) {
					sb.append(p.getUUID()).append('=').append(serialize(SkillManager.save(p))).append('\n');
				}
			}
			Files.writeString(saveFile, sb.toString());
		} catch (IOException e) {
			LOGGER.error("Failed to save limitskills data", e);
		}
	}

	// Format: mining:x:lv|combat:x:lv|...|perks:mining:tier:choice,...
	private static String serialize(CompoundTag tag) {
		StringBuilder sb = new StringBuilder();
		for (SkillType t : SkillType.values()) {
			if (!tag.contains(t.id)) continue;
			CompoundTag st = tag.getCompound(t.id);
			if (sb.length() > 0) sb.append('|');
			sb.append(t.id).append(':').append(st.getDouble("xp")).append(':').append(st.getInt("level"));
			if (st.contains("perks")) {
				CompoundTag perks = st.getCompound("perks");
				for (String k : perks.getAllKeys()) {
					sb.append("|perk:").append(t.id).append(':').append(k).append(':').append(perks.getInt(k));
				}
			}
		}
		return sb.toString();
	}

	private static CompoundTag deserialize(String s) {
		CompoundTag tag = new CompoundTag();
		for (String part : s.split("\\|")) {
			String[] p = part.split(":");
			if (p.length < 3) continue;
			if (p[0].equals("perk") && p.length == 4) {
				SkillType t = SkillType.byId(p[1]);
				if (t == null) continue;
				CompoundTag st = tag.contains(t.id) ? tag.getCompound(t.id) : new CompoundTag();
				CompoundTag perks = st.contains("perks") ? st.getCompound("perks") : new CompoundTag();
				try { perks.putInt(p[2], Integer.parseInt(p[3])); } catch (NumberFormatException ignored) {}
				st.put("perks", perks);
				tag.put(t.id, st);
			} else if (p.length == 3) {
				SkillType t = SkillType.byId(p[0]);
				if (t == null) continue;
				CompoundTag st = new CompoundTag();
				try {
					st.putDouble("xp", Double.parseDouble(p[1]));
					st.putInt("level", Integer.parseInt(p[2]));
				} catch (NumberFormatException ignored) {}
				tag.put(t.id, st);
			}
		}
		return tag;
	}
}
