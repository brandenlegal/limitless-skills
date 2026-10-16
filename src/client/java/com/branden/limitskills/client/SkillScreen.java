package com.branden.limitskills.client;

import com.branden.limitskills.Perks;
import com.branden.limitskills.SkillManager;
import com.branden.limitskills.SkillType;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.ColorBackground;
import com.daqem.uilib.gui.component.text.TextComponent;
import com.daqem.uilib.gui.widget.ButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

/**
 * Main skill screen using ui-lib (proven in Limitless Enchants v2.5.3).
 */
@Environment(EnvType.CLIENT)
public class SkillScreen extends AbstractScreen {
	private SkillType selectedSkill = SkillType.MINING;

	public SkillScreen() {
		super(Component.literal("Limitless Skills"));
	}

	@Override
	protected void init() {
		super.init();
		this.setBackground(new ColorBackground(0xAA000000));

		int centerX = this.width / 2;
		int y = 30;

		this.addComponent(new TextComponent(centerX, y, Component.literal("§6§lLimitless Skills")));
		y += 20;

		int totalLevel = ClientSkillCache.getTotalLevel();
		this.addComponent(new TextComponent(centerX, y,
			Component.literal("§7Total Level: §a" + totalLevel)));
		y += 25;

		int btnX = centerX - (SkillType.values().length * 95) / 2;
		for (SkillType type : SkillType.values()) {
			int level = ClientSkillCache.getLevel(type);
			ButtonWidget btn = new ButtonWidget(btnX, y, 90, 20,
				Component.literal(type.displayName + " [" + level + "]"),
				b -> {
					selectedSkill = type;
					this.rebuild();
				});
			this.addWidget(btn);
			btnX += 95;
		}
		y += 30;

		int level = ClientSkillCache.getLevel(selectedSkill);
		double xp = ClientSkillCache.getXp(selectedSkill);
		double progress = SkillManager.progressToNext(xp);

		this.addComponent(new TextComponent(centerX, y,
			Component.literal("§l" + selectedSkill.displayName + " §7- Level " + level)));
		y += 15;

		this.addComponent(new TextComponent(centerX, y,
			Component.literal("§7Progress: §a" + String.format("%.1f%%", progress * 100))));
		y += 20;

		this.addComponent(new TextComponent(centerX, y, Component.literal("§6§lPerks:")));
		y += 15;

		for (String perk : getPerkDescriptions(selectedSkill, level)) {
			this.addComponent(new TextComponent(centerX, y,
				Component.literal("§7• " + perk)));
			y += 12;
		}
	}

	private void rebuild() {
		this.clearComponents();
		this.init();
	}

	private String[] getPerkDescriptions(SkillType type, int level) {
		return switch (type) {
			case MINING -> new String[]{
				"Mining speed: +" + String.format("%.0f%%", Perks.miningSpeedBonus(level) * 100),
				"Double drop: " + String.format("%.1f%%", Perks.doubleDropChance(level) * 100)
			};
			case COMBAT -> new String[]{
				"Damage: +" + String.format("%.1f%%", Perks.damageBonus(level) * 100),
				"Crit: +" + String.format("%.1f%%", Perks.critChanceBonus(level) * 100)
			};
			case FARMING -> new String[]{
				"Bonus yield: +" + Perks.bonusCropYield(level)
			};
			case SURVIVAL -> new String[]{
				"Damage reduction: " + String.format("%.1f%%", Perks.damageReduction(level) * 100),
				"Bonus health: +" + Perks.bonusHealth(level)
			};
			case ARCANE -> new String[]{
				"Enchant discount: " + String.format("%.0f%%", Perks.enchantCostReduction(level) * 100),
				"Potion duration: +" + String.format("%.0f%%", Perks.potionDurationBonus(level) * 100)
			};
		};
	}
}
