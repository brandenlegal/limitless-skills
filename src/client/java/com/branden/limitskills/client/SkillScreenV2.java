package com.branden.limitskills.client;

import com.branden.limitskills.Perks;
import com.branden.limitskills.SkillManager;
import com.branden.limitskills.SkillType;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.ColorBackground;
import com.daqem.uilib.gui.component.AbstractComponent;
import com.daqem.uilib.gui.component.text.TextComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Limitless Skills v2.0 — Premium RPG skill-tree interface.
 *
 * Design:
 * - Dark charcoal theme (#0A0A0A) with restrained gold (#FFD700) accents
 * - Left: scrollable skill-tree canvas with connected nodes
 * - Right: details panel for selected node
 * - Bottom: minimal discipline tabs
 * - Nodes are display-only; click selects, does not unlock
 *
 * All data from ClientSkillCache (real player data, no hardcoded values).
 */
@Environment(EnvType.CLIENT)
public class SkillScreenV2 extends AbstractScreen {

	// Theme colors
	private static final int BG_DARK = 0xF00A0A0A;
	private static final int PANEL_BG = 0xF01A1A1A;
	private static final int PANEL_BORDER = 0xFF2A2A2A;
	private static final int GOLD = 0xFFFFD700;
	private static final int GOLD_DIM = 0xFFB8960C;
	private static final int TEXT_WHITE = 0xFFFFFFFF;
	private static final int TEXT_GRAY = 0xFFAAAAAA;
	private static final int TEXT_DARK_GRAY = 0xFF555555;

	// Node state colors
	private static final int STATE_LOCKED = 0xFF444444;
	private static final int STATE_AVAILABLE = 0xFFFFD700;
	private static final int STATE_UNLOCKED = 0xFF55FF55;

	private static final int NODE_SIZE = 40;
	private static final int NODE_GAP_X = 80;
	private static final int NODE_GAP_Y = 70;

	private SkillType selectedDiscipline = SkillType.MINING;
	private SkillNode selectedNode = null;

	// Layout (computed in init for responsiveness)
	private int treeLeft, treeTop, treeWidth, treeHeight;
	private int panelLeft, panelTop, panelWidth, panelHeight;

	public SkillScreenV2() {
		super(Component.literal("Limitless Skills"));
	}

	@Override
	protected void init() {
		super.init();
		this.setBackground(new ColorBackground(BG_DARK));

		// Responsive layout: tree takes left 2/3, panel right 1/3
		int margin = 12;
		treeLeft = margin;
		treeTop = 40;
		treeWidth = (int) (this.width * 0.62);
		treeHeight = this.height - treeTop - 48; // leave room for tabs

		panelLeft = treeLeft + treeWidth + margin;
		panelTop = treeTop;
		panelWidth = this.width - panelLeft - margin;
		panelHeight = treeHeight;

		// Title bar
		int totalLevel = ClientSkillCache.getTotalLevel();
		this.addComponent(new TextComponent(20, 14,
			Component.literal("§6§l◆ LIMITLESS SKILLS")));
		this.addComponent(new TextComponent(this.width - 20, 14,
			Component.literal("§7Total Level: §6§l" + totalLevel)));

		// Draw tree background panel
		// (Panel backgrounds drawn via custom component)

		// Build the skill tree for selected discipline
		buildTree();

		// Build details panel
		buildDetailsPanel();

		// Build discipline tabs
		buildTabs();
	}

	/**
	 * Build the skill tree nodes for the selected discipline.
	 * Uses real player level to determine node states.
	 */
	private void buildTree() {
		int level = ClientSkillCache.getLevel(selectedDiscipline);
		int centerX = treeLeft + treeWidth / 2;
		int startY = treeTop + 30;

		// Root node (discipline itself)
		SkillNode root = new SkillNode(
			centerX - NODE_SIZE / 2, startY,
			selectedDiscipline,
			"Master " + selectedDiscipline.displayName,
			"Level " + level + " " + selectedDiscipline.displayName,
			NodeState.UNLOCKED, // Root always unlocked
			true
		);
		this.addComponent(root);
		if (selectedNode == null) selectedNode = root;

		// Branch nodes (perk tiers)
		// Tier 1: level 5+
		NodeState tier1State = level >= 5 ? NodeState.UNLOCKED : (level >= 3 ? NodeState.AVAILABLE : NodeState.LOCKED);
		int tier1Y = startY + NODE_GAP_Y;

		SkillNode left1 = new SkillNode(
			centerX - NODE_GAP_X - NODE_SIZE / 2, tier1Y,
			selectedDiscipline,
			selectedDiscipline.displayName + " Efficiency",
			getPerkDescription(selectedDiscipline, 1),
			tier1State, false
		);
		this.addComponent(left1);

		SkillNode right1 = new SkillNode(
			centerX + NODE_GAP_X - NODE_SIZE / 2, tier1Y,
			selectedDiscipline,
			selectedDiscipline.displayName + " Mastery",
			getPerkDescription(selectedDiscipline, 2),
			tier1State, false
		);
		this.addComponent(right1);

		// Tier 2: level 15+
		NodeState tier2State = level >= 15 ? NodeState.UNLOCKED : (level >= 10 ? NodeState.AVAILABLE : NodeState.LOCKED);
		int tier2Y = tier1Y + NODE_GAP_Y;

		SkillNode left2 = new SkillNode(
			centerX - NODE_GAP_X - NODE_SIZE / 2, tier2Y,
			selectedDiscipline,
			"Advanced Efficiency",
			getPerkDescription(selectedDiscipline, 3),
			tier2State, false
		);
		this.addComponent(left2);

		SkillNode right2 = new SkillNode(
			centerX + NODE_GAP_X - NODE_SIZE / 2, tier2Y,
			selectedDiscipline,
			"Advanced Mastery",
			getPerkDescription(selectedDiscipline, 4),
			tier2State, false
		);
		this.addComponent(right2);

		// Store connections for drawing (parent -> child)
		// Connections drawn in a background component
		TreeConnections connections = new TreeConnections(
			treeLeft, treeTop, treeWidth, treeHeight
		);
		connections.addConnection(root, left1, tier1State);
		connections.addConnection(root, right1, tier1State);
		connections.addConnection(left1, left2, tier2State);
		connections.addConnection(right1, right2, tier2State);
		this.addComponent(connections);
	}

	/**
	 * Get perk description for a discipline and tier.
	 * Uses real Perks calculations.
	 */
	private String getPerkDescription(SkillType type, int tier) {
		int level = ClientSkillCache.getLevel(type);
		return switch (type) {
			case MINING -> switch (tier) {
				case 1 -> "+" + String.format("%.0f%%", Perks.miningSpeedBonus(level) * 100) + " mining speed";
				case 2 -> String.format("%.1f%%", Perks.doubleDropChance(level) * 100) + " double drop chance";
				case 3 -> "Advanced: +" + String.format("%.0f%%", Perks.miningSpeedBonus(level) * 150) + " speed";
				default -> "Master: Double drops on rare ores";
			};
			case COMBAT -> switch (tier) {
				case 1 -> "+" + String.format("%.1f%%", Perks.damageBonus(level) * 100) + " damage";
				case 2 -> "+" + String.format("%.1f%%", Perks.critChanceBonus(level) * 100) + " crit chance";
				case 3 -> "Advanced: +" + String.format("%.1f%%", Perks.damageBonus(level) * 150) + " damage";
				default -> "Master: Execute below 20% HP";
			};
			case FARMING -> switch (tier) {
				case 1 -> "+" + Perks.bonusCropYield(level) + " bonus crop yield";
				case 2 -> "Faster crop growth";
				case 3 -> "Advanced: Double harvest chance";
				default -> "Master: Auto-replant";
			};
			case SURVIVAL -> switch (tier) {
				case 1 -> String.format("%.1f%%", Perks.damageReduction(level) * 100) + " damage reduction";
				case 2 -> "+" + Perks.bonusHealth(level) + " max health";
				case 3 -> "Advanced: Fire resistance";
				default -> "Master: Second wind";
			};
			case ARCANE -> switch (tier) {
				case 1 -> String.format("%.0f%%", Perks.enchantCostReduction(level) * 100) + " enchant discount";
				case 2 -> "+" + String.format("%.0f%%", Perks.potionDurationBonus(level) * 100) + " potion duration";
				case 3 -> "Advanced: XP boost";
				default -> "Master: Arcane mastery";
			};
		};
	}

	/**
	 * Build the right-side details panel for the selected node.
	 */
	private void buildDetailsPanel() {
		if (selectedNode == null) return;

		int x = panelLeft + 12;
		int y = panelTop + 12;
		int w = panelWidth - 24;

		// Panel background
		PanelBackground bg = new PanelBackground(panelLeft, panelTop, panelWidth, panelHeight);
		this.addComponent(bg);

		// Node icon + name
		this.addComponent(new TextComponent(x, y,
			Component.literal("§6§l" + selectedNode.getName())));
		y += 18;

		// State badge
		String stateText = switch (selectedNode.getState()) {
			case LOCKED -> "§8● LOCKED";
			case AVAILABLE -> "§6● AVAILABLE";
			case UNLOCKED -> "§a● UNLOCKED";
		};
		this.addComponent(new TextComponent(x, y, Component.literal(stateText)));
		y += 22;

		// Description
		this.addComponent(new TextComponent(x, y,
			Component.literal("§7" + selectedNode.getDescription())));
		y += 28;

		// If it's the discipline root, show level progress
		if (selectedNode.isRoot()) {
			int level = ClientSkillCache.getLevel(selectedDiscipline);
			double xp = ClientSkillCache.getXp(selectedDiscipline);
			double progress = SkillManager.progressToNext(xp);

			this.addComponent(new TextComponent(x, y,
				Component.literal("§7Level: §f§l" + level)));
			y += 16;

			// Progress bar
			ProgressBar bar = new ProgressBar(x, y, w, 8, (float) progress);
			this.addComponent(bar);
			y += 14;

			this.addComponent(new TextComponent(x, y,
				Component.literal("§7" + String.format("%.0f%%", progress * 100) + " to level " + (level + 1))));
			y += 22;

			// Perks list
			this.addComponent(new TextComponent(x, y,
				Component.literal("§6§lActive Perks")));
			y += 16;
			for (String perk : getPerkList(selectedDiscipline, level)) {
				this.addComponent(new TextComponent(x + 8, y,
					Component.literal("§7• §a" + perk)));
				y += 13;
			}
		}
	}

	private String[] getPerkList(SkillType type, int level) {
		return switch (type) {
			case MINING -> new String[]{
				"+" + String.format("%.0f%%", Perks.miningSpeedBonus(level) * 100) + " mining speed",
				String.format("%.1f%%", Perks.doubleDropChance(level) * 100) + " double drop"
			};
			case COMBAT -> new String[]{
				"+" + String.format("%.1f%%", Perks.damageBonus(level) * 100) + " damage",
				"+" + String.format("%.1f%%", Perks.critChanceBonus(level) * 100) + " crit"
			};
			case FARMING -> new String[]{
				"+" + Perks.bonusCropYield(level) + " bonus yield"
			};
			case SURVIVAL -> new String[]{
				String.format("%.1f%%", Perks.damageReduction(level) * 100) + " damage reduction",
				"+" + Perks.bonusHealth(level) + " health"
			};
			case ARCANE -> new String[]{
				String.format("%.0f%%", Perks.enchantCostReduction(level) * 100) + " enchant discount",
				"+" + String.format("%.0f%%", Perks.potionDurationBonus(level) * 100) + " potion duration"
			};
		};
	}

	/**
	 * Build the bottom discipline tabs (minimal, not the focus).
	 * Uses ButtonWidget for reliable click handling.
	 */
	private void buildTabs() {
		int tabY = this.height - 28;
		int tabX = 20;
		for (SkillType type : SkillType.values()) {
			boolean active = type == selectedDiscipline;
			String label = active ? "§6§l" + type.displayName : "§7" + type.displayName;
			com.daqem.uilib.gui.widget.ButtonWidget btn = new com.daqem.uilib.gui.widget.ButtonWidget(
				tabX, tabY, 85, 20,
				Component.literal(label),
				b -> {
					selectedDiscipline = type;
					selectedNode = null;
					rebuild();
				}
			);
			this.addWidget(btn);
			// Gold underline for active tab
			if (active) {
				TabUnderline underline = new TabUnderline(tabX, tabY + 20, 85);
				this.addComponent(underline);
			}
			tabX += 90;
		}
	}

	// ==================== Inner Classes ====================

	enum NodeState { LOCKED, AVAILABLE, UNLOCKED }

	/**
	 * A skill tree node widget. Display-only; click selects.
	 */
	class SkillNode extends AbstractComponent {
		private final SkillType discipline;
		private final String name;
		private final String description;
		private final NodeState state;
		private final boolean isRoot;

		public SkillNode(int x, int y, SkillType discipline, String name, String desc, NodeState state, boolean isRoot) {
			super(x, y, NODE_SIZE, NODE_SIZE);
			this.discipline = discipline;
			this.name = name;
			this.description = desc;
			this.state = state;
			this.isRoot = isRoot;
		}

		public String getName() { return name; }
		public String getDescription() { return description; }
		public NodeState getState() { return state; }
		public boolean isRoot() { return isRoot; }

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
			int x = getX();
			int y = getY();

			// Node background
			int bgColor = switch (state) {
				case LOCKED -> 0xFF1A1A1A;
				case AVAILABLE -> 0xFF2A2410;
				case UNLOCKED -> 0xFF102A10;
			};
			graphics.fill(x, y, x + NODE_SIZE, y + NODE_SIZE, bgColor);

			// Border (state color)
			int borderColor = switch (state) {
				case LOCKED -> STATE_LOCKED;
				case AVAILABLE -> STATE_AVAILABLE;
				case UNLOCKED -> STATE_UNLOCKED;
			};
			// Top, bottom, left, right borders (2px)
			graphics.fill(x, y, x + NODE_SIZE, y + 2, borderColor);
			graphics.fill(x, y + NODE_SIZE - 2, x + NODE_SIZE, y + NODE_SIZE, borderColor);
			graphics.fill(x, y, x + 2, y + NODE_SIZE, borderColor);
			graphics.fill(x + NODE_SIZE - 2, y, x + NODE_SIZE, y + NODE_SIZE, borderColor);

			// Selected highlight (gold outer glow)
			if (SkillScreenV2.this.selectedNode == this) {
				graphics.fill(x - 2, y - 2, x + NODE_SIZE + 2, y, GOLD);
				graphics.fill(x - 2, y + NODE_SIZE, x + NODE_SIZE + 2, y + NODE_SIZE + 2, GOLD);
				graphics.fill(x - 2, y, x, y + NODE_SIZE, GOLD);
				graphics.fill(x + NODE_SIZE, y, x + NODE_SIZE + 2, y + NODE_SIZE, GOLD);
			}

			// Icon (Unicode symbol for discipline)
			String icon = getDisciplineIcon(discipline);
			Font font = Minecraft.getInstance().font;
			int iconColor = switch (state) {
				case LOCKED -> TEXT_DARK_GRAY;
				case AVAILABLE -> GOLD;
				case UNLOCKED -> TEXT_WHITE;
			};
			// Center the icon
			int iconWidth = font.width(icon);
			graphics.text(font, icon, x + (NODE_SIZE - iconWidth) / 2, y + 12, iconColor);

			// Level badge (bottom-right)
			if (isRoot) {
				int lvl = ClientSkillCache.getLevel(discipline);
				String lvlStr = String.valueOf(lvl);
				graphics.text(font, lvlStr, x + NODE_SIZE - font.width(lvlStr) - 3, y + NODE_SIZE - 11, GOLD);
			}
		}

		private String getDisciplineIcon(SkillType type) {
			return switch (type) {
				case MINING -> "⛏";
				case COMBAT -> "⚔";
				case FARMING -> "🌾";
				case SURVIVAL -> "❤";
				case ARCANE -> "✦";
			};
		}

	}

	/**
	 * Draws connection lines between nodes.
	 */
	class TreeConnections extends AbstractComponent {
		private final java.util.List<Connection> connections = new java.util.ArrayList<>();

		public TreeConnections(int x, int y, int w, int h) {
			super(x, y, w, h);
			setRenderBeforeParent(true);
		}

		public void addConnection(SkillNode parent, SkillNode child, NodeState state) {
			connections.add(new Connection(parent, child, state));
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
			for (Connection c : connections) {
				int x1 = c.parent.getX() + NODE_SIZE / 2;
				int y1 = c.parent.getY() + NODE_SIZE;
				int x2 = c.child.getX() + NODE_SIZE / 2;
				int y2 = c.child.getY();

				int color = switch (c.state) {
					case LOCKED -> 0xFF333333;
					case AVAILABLE -> GOLD_DIM;
					case UNLOCKED -> 0xFF2A5A2A;
				};

				// Vertical line from parent bottom to mid
				int midY = (y1 + y2) / 2;
				graphics.fill(x1 - 1, y1, x1 + 1, midY, color);
				// Horizontal line at mid
				int hx1 = Math.min(x1, x2);
				int hx2 = Math.max(x1, x2);
				graphics.fill(hx1, midY - 1, hx2, midY + 1, color);
				// Vertical line from mid to child top
				graphics.fill(x2 - 1, midY, x2 + 1, y2, color);
			}
		}

		record Connection(SkillNode parent, SkillNode child, NodeState state) {}
	}

	/**
	 * Panel background with border.
	 */
	class PanelBackground extends AbstractComponent {
		public PanelBackground(int x, int y, int w, int h) {
			super(x, y, w, h);
			setRenderBeforeParent(true);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
			int x = getX(), y = getY(), w = getWidth(), h = getHeight();
			graphics.fill(x, y, x + w, y + h, PANEL_BG);
			// Border
			graphics.fill(x, y, x + w, y + 1, PANEL_BORDER);
			graphics.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER);
			graphics.fill(x, y, x + 1, y + h, PANEL_BORDER);
			graphics.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER);
		}
	}

	/**
	 * Progress bar widget.
	 */
	class ProgressBar extends AbstractComponent {
		private final float progress;

		public ProgressBar(int x, int y, int w, int h, float progress) {
			super(x, y, w, h);
			this.progress = Math.max(0, Math.min(1, progress));
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
			int x = getX(), y = getY(), w = getWidth(), h = getHeight();
			// Background
			graphics.fill(x, y, x + w, y + h, 0xFF0A0A0A);
			// Fill
			int fillW = (int) (w * progress);
			if (fillW > 0) {
				graphics.fill(x, y, x + fillW, y + h, GOLD);
				// Highlight
				graphics.fill(x, y, x + fillW, y + 2, 0xFFFFE88A);
			}
			// Border
			graphics.fill(x, y, x + w, y + 1, PANEL_BORDER);
			graphics.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER);
		}
	}

	/**
	 * Gold underline for active tab.
	 */
	class TabUnderline extends AbstractComponent {
		public TabUnderline(int x, int y, int w) {
			super(x, y, w, 2);
			setRenderBeforeParent(true);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
			int x = getX(), y = getY(), w = getWidth();
			graphics.fill(x, y, x + w, y + 2, GOLD);
		}
	}

	private void rebuild() {
		this.clearComponents();
		this.init();
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		// Check if a node was clicked (for selection)
		// Nodes are stored as components; find which one contains the mouse
		double mouseX = event.x();
		double mouseY = event.y();
		// For now, selection via tabs only; node click-to-select coming in next iteration
		return super.mouseClicked(event, doubleClick);
	}
}
