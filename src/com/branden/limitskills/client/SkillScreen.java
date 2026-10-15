package com.branden.limitskills.client;

import com.branden.limitskills.SkillData;
import com.branden.limitskills.SkillType;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Skill tree overview — the ONE screen for Branden's art direction pass.
 */
public class SkillScreen extends BaseOwoScreen<FlowLayout> {

	public SkillScreen() {
		super(net.minecraft.network.chat.Component.literal("Skills"));
	}

	@Override
	protected OwoUIAdapter<FlowLayout> createAdapter() {
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	protected void build(FlowLayout root) {
		root.gap(8);
		root.surface(Surface.VANILLA_TRANSLUCENT)
			.horizontalAlignment(HorizontalAlignment.CENTER)
			.verticalAlignment(VerticalAlignment.CENTER)
			.padding(Insets.of(16));

		// Title
		root.child(Components.label(net.minecraft.network.chat.Component.literal("§6§lSKILLS"))
			.horizontalTextAlignment(HorizontalAlignment.CENTER)
			.margins(Insets.of(0, 8, 0, 0)));

		// Skill cards
		for (SkillType type : SkillType.values()) {
			root.child(buildSkillCard(type));
		}

		// Close hint
		root.child(Components.label(net.minecraft.network.chat.Component.literal("§7Press G or ESC to close"))
			.horizontalTextAlignment(HorizontalAlignment.CENTER)
			.margins(Insets.of(8, 0, 0, 0)));
	}

	private io.wispforest.owo.ui.core.Component buildSkillCard(SkillType type) {
		int level = ClientSkillCache.getLevel(type);
		double xp = ClientSkillCache.getXp(type);
		double needed = SkillData.xpForLevel(level);
		double progress = level >= 50 ? 1.0 : Math.min(xp / needed, 1.0);

		FlowLayout card = Containers.verticalFlow(Sizing.fill(90), Sizing.content());
		card.gap(4);
		card.surface(Surface.DARK_PANEL)
			.padding(Insets.of(10));

		// Header: icon + name + level
		FlowLayout header = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
		header.gap(8);
		header.verticalAlignment(VerticalAlignment.CENTER);
		header.child(Components.item(iconFor(type)));
		header.child(Components.label(net.minecraft.network.chat.Component.literal("§e§l" + type.displayName))
			.verticalTextAlignment(VerticalAlignment.CENTER));
		header.child(Components.label(net.minecraft.network.chat.Component.literal("§7Lv §f" + level))
			.verticalTextAlignment(VerticalAlignment.CENTER)
			.margins(Insets.of(0, 0, 0, 8)));
		card.child(header);

		// XP bar
		FlowLayout barBg = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(6));
		barBg.surface(Surface.TOOLTIP);
		BoxComponent fill = new BoxComponent(Sizing.fill((int) (progress * 100)), Sizing.fill(100));
		fill.color(colorFor(type));
		barBg.child(fill);
		card.child(barBg);

		// XP text
		String xpText = level >= 50 ? "§6§lMAX" : String.format("§7%.0f / %.0f XP", xp, needed);
		card.child(Components.label(net.minecraft.network.chat.Component.literal(xpText)));

		// Click for perks (placeholder)
		card.child(Components.label(net.minecraft.network.chat.Component.literal("§8Click to view perks →")));

		return card;
	}

	private ItemStack iconFor(SkillType type) {
		return switch (type) {
			case MINING -> new ItemStack(Items.DIAMOND_PICKAXE);
			case COMBAT -> new ItemStack(Items.DIAMOND_SWORD);
			case FARMING -> new ItemStack(Items.WHEAT);
			case SURVIVAL -> new ItemStack(Items.COOKED_BEEF);
			case ARCANE -> new ItemStack(Items.ENCHANTING_TABLE);
		};
	}

	private Color colorFor(SkillType type) {
		return switch (type) {
			case MINING -> Color.ofRgb(0x4A90D9);   // blue
			case COMBAT -> Color.ofRgb(0xD94A4A);   // red
			case FARMING -> Color.ofRgb(0x7ED94A);  // green
			case SURVIVAL -> Color.ofRgb(0xD9A44A); // orange
			case ARCANE -> Color.ofRgb(0x9D4AD9);   // purple
		};
	}
}
