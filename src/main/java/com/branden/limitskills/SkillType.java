package com.branden.limitskills;

/**
 * The five skill trees.
 */
public enum SkillType {
	MINING("mining", "Mining", 0xF5A623),
	COMBAT("combat", "Combat", 0xE74C3C),
	FARMING("farming", "Farming", 0x2ECC71),
	SURVIVAL("survival", "Survival", 0x3498DB),
	ARCANE("arcane", "Arcane", 0x9B59B6);

	public final String id;
	public final String displayName;
	public final int color;

	SkillType(String id, String displayName, int color) {
		this.id = id;
		this.displayName = displayName;
		this.color = color;
	}

	public static SkillType byId(String id) {
		for (SkillType t : values()) {
			if (t.id.equals(id)) return t;
		}
		return null;
	}
}
