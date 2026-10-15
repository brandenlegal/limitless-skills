package com.branden.limitskills;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

/**
 * Per-player skill XP and levels. Persisted to player NBT.
 */
public class SkillData {
	private final Map<SkillType, Double> xp = new EnumMap<>(SkillType.class);
	private final Map<SkillType, Integer> levels = new EnumMap<>(SkillType.class);
	// Perk choices: skill -> (tier -> choice index 0 or 1)
	private final Map<SkillType, Map<Integer, Integer>> perkChoices = new EnumMap<>(SkillType.class);

	public SkillData() {
		for (SkillType t : SkillType.values()) {
			xp.put(t, 0.0);
			levels.put(t, 0);
			perkChoices.put(t, new java.util.HashMap<>());
		}
	}

	public double getXp(SkillType type) {
		return xp.getOrDefault(type, 0.0);
	}

	public int getLevel(SkillType type) {
		return levels.getOrDefault(type, 0);
	}

	public void setXp(SkillType type, double value) {
		xp.put(type, value);
	}

	public void setLevel(SkillType type, int value) {
		levels.put(type, value);
	}

	public int getPerkChoice(SkillType type, int tier) {
		return perkChoices.get(type).getOrDefault(tier, -1);
	}

	public void setPerkChoice(SkillType type, int tier, int choice) {
		perkChoices.get(type).put(tier, choice);
	}

	public boolean hasPerkChoice(SkillType type, int tier) {
		return perkChoices.get(type).containsKey(tier);
	}

	/** XP required to go from level to level+1. */
	public static double xpForLevel(int level) {
		return 100.0 * Math.pow(level + 1, 1.6);
	}

	public CompoundTag toNbt() {
		CompoundTag tag = new CompoundTag();
		for (SkillType t : SkillType.values()) {
			CompoundTag st = new CompoundTag();
			st.putDouble("xp", getXp(t));
			st.putInt("level", getLevel(t));
			CompoundTag perks = new CompoundTag();
			for (Map.Entry<Integer, Integer> e : perkChoices.get(t).entrySet()) {
				perks.putInt(String.valueOf(e.getKey()), e.getValue());
			}
			st.put("perks", perks);
			tag.put(t.id, st);
		}
		return tag;
	}

	public static SkillData fromNbt(CompoundTag tag) {
		SkillData data = new SkillData();
		if (tag == null) return data;
		for (SkillType t : SkillType.values()) {
			if (!tag.contains(t.id)) continue;
			CompoundTag st = tag.getCompound(t.id);
			data.setXp(t, st.getDouble("xp"));
			data.setLevel(t, st.getInt("level"));
			if (st.contains("perks")) {
				CompoundTag perks = st.getCompound("perks");
				for (String key : perks.getAllKeys()) {
					try {
						data.setPerkChoice(t, Integer.parseInt(key), perks.getInt(key));
					} catch (NumberFormatException ignored) {}
				}
			}
		}
		return data;
	}
}
