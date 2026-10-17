package com.branden.limitskills.client;

import com.branden.limitskills.SkillType;
import java.util.Arrays;

/**
 * Client-side cache of skill data, synced from server.
 */
public final class ClientSkillCache {
	private static final double[] xp = new double[SkillType.values().length];
	private static final int[] levels = new int[SkillType.values().length];

	public static void update(double[] newXp, int[] newLevels) {
		clear();
		System.arraycopy(newXp, 0, xp, 0, Math.min(newXp.length, xp.length));
		System.arraycopy(newLevels, 0, levels, 0, Math.min(newLevels.length, levels.length));
	}

	public static void clear() {
		Arrays.fill(xp, 0.0);
		Arrays.fill(levels, 0);
	}

	public static double getXp(SkillType type) {
		return xp[type.ordinal()];
	}

	public static int getLevel(SkillType type) {
		return levels[type.ordinal()];
	}

	public static int getTotalLevel() {
		int total = 0;
		for (int l : levels) total += l;
		return total;
	}
}
