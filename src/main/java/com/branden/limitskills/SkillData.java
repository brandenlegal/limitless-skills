package com.branden.limitskills;

import net.minecraft.nbt.CompoundTag;

/**
 * Per-player skill data: XP and levels for each skill type.
 */
public class SkillData {
	private final double[] xp = new double[SkillType.values().length];
	private final int[] levels = new int[SkillType.values().length];

	public double getXp(SkillType type) {
		return xp[type.ordinal()];
	}

	public int getLevel(SkillType type) {
		return levels[type.ordinal()];
	}

	public void addXp(SkillType type, double amount) {
		int i = type.ordinal();
		xp[i] += amount;
		levels[i] = SkillManager.xpToLevel(xp[i]);
	}

	public double[] getAllXp() {
		return xp.clone();
	}

	public int[] getAllLevels() {
		return levels.clone();
	}

	public void fromArrays(double[] xpArr, int[] lvlArr) {
		System.arraycopy(xpArr, 0, xp, 0, Math.min(xpArr.length, xp.length));
		System.arraycopy(lvlArr, 0, levels, 0, Math.min(lvlArr.length, levels.length));
	}

	public CompoundTag toNbt() {
		CompoundTag tag = new CompoundTag();
		for (SkillType t : SkillType.values()) {
			tag.putDouble("xp_" + t.id, xp[t.ordinal()]);
			tag.putInt("level_" + t.id, levels[t.ordinal()]);
		}
		return tag;
	}

	public void fromNbt(CompoundTag tag) {
		for (SkillType t : SkillType.values()) {
			xp[t.ordinal()] = tag.getDouble("xp_" + t.id).orElse(0.0);
			levels[t.ordinal()] = tag.getInt("level_" + t.id).orElse(0);
		}
	}

	public int getTotalLevel() {
		int total = 0;
		for (int l : levels) total += l;
		return total;
	}
}
