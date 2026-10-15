package com.branden.limitskills;

/**
 * The five skill trees.
 */
public enum SkillType {
	MINING("mining", "Mining"),
	COMBAT("combat", "Combat"),
	FARMING("farming", "Farming"),
	SURVIVAL("survival", "Survival"),
	ARCANE("arcane", "Arcane");

	public final String id;
	public final String displayName;

	SkillType(String id, String displayName) {
		this.id = id;
		this.displayName = displayName;
	}

	public static SkillType byId(String id) {
		for (SkillType t : values()) {
			if (t.id.equals(id)) return t;
		}
		return null;
	}
}
