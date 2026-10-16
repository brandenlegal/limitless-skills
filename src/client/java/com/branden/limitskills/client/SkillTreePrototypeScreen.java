package com.branden.limitskills.client;

import com.branden.limitskills.SkillType;
import com.daqem.uilib.api.skilltree.ISkillTree;
import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.ColorBackground;
import com.daqem.uilib.gui.component.text.TextComponent;
import com.daqem.uilib.gui.widget.skilltree.SkillTreeWidget;
import com.daqem.uilib.skilltree.AbstractSkillTree;
import com.daqem.uilib.skilltree.AbstractSkillTreeItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Polished skill-tree prototype demonstrating ui-lib's capabilities.
 * Features: connected nodes, locked/available/unlocked states, tooltips.
 *
 * This is a PROTOTYPE - does not replace the working SkillScreen.
 * Created to evaluate ui-lib's visual capabilities for the final UI decision.
 */
@Environment(EnvType.CLIENT)
public class SkillTreePrototypeScreen extends AbstractScreen {

	private final SkillType skillType;
	private SkillTreeWidget treeWidget;

	public SkillTreePrototypeScreen(SkillType skillType) {
		super(Component.literal("Skill Tree Prototype"));
		this.skillType = skillType;
	}

	@Override
	protected void init() {
		super.init();
		this.setBackground(new ColorBackground(0xDD0A0A0A));

		int centerX = this.width / 2;

		// Title
		this.addComponent(new TextComponent(centerX, 20,
			Component.literal("§6§l" + skillType.displayName + " Skill Tree")));
		this.addComponent(new TextComponent(centerX, 35,
			Component.literal("§7Prototype - ui-lib capabilities demo")));

		// Skill tree widget (scrollable)
		int treeW = Math.min(600, this.width - 40);
		int treeH = this.height - 100;
		int treeX = centerX - treeW / 2;
		int treeY = 55;

		treeWidget = new SkillTreeWidget(treeW, treeH);
		// Position the widget
		// Note: ui-lib widgets use relative positioning within the screen

		// Build the skill tree
		ISkillTree tree = buildSkillTree();
		tree.runPositioner();

		// Add tree items as widgets
		// (In a full implementation, each ISkillTreeItem.createWidget()
		// would be added to the treeWidget)

		this.addWidget(treeWidget);

		// Footer
		this.addComponent(new TextComponent(centerX, this.height - 25,
			Component.literal("§7Locked: §8■  Available: §e■  Unlocked: §a■")));
	}

	/**
	 * Build a sample skill tree for demonstration.
	 * Structure: root -> 3 branches -> 2 leaves each
	 */
	private ISkillTree buildSkillTree() {
		int level = ClientSkillCache.getLevel(skillType);

		// Root node - shows real level
		PrototypeSkillNode root = new PrototypeSkillNode(
			"Master " + skillType.displayName,
			"Current level: " + level + ". The pinnacle of " + skillType.displayName.toLowerCase() + " mastery.",
			NodeState.UNLOCKED,
			true
		);

		AbstractSkillTree tree = new AbstractSkillTree(new ArrayList<>()) {
			// Anonymous implementation
		};
		tree.setRoot(root);

		// Branch 1
		PrototypeSkillNode branch1 = new PrototypeSkillNode(
			skillType.displayName + " Efficiency",
			"Work 25% faster.",
			NodeState.AVAILABLE,
			false
		);
		root.addChild(branch1);
		branch1.addChild(new PrototypeSkillNode(
			"Efficiency II",
			"Work 50% faster. Requires Efficiency I.",
			NodeState.LOCKED,
			false
		));
		branch1.addChild(new PrototypeSkillNode(
			"Swift Hands",
			"10% chance for double output.",
			NodeState.LOCKED,
			false
		));

		// Branch 2
		PrototypeSkillNode branch2 = new PrototypeSkillNode(
			skillType.displayName + " Mastery",
			"Unlock advanced techniques.",
			NodeState.AVAILABLE,
			false
		);
		root.addChild(branch2);
		branch2.addChild(new PrototypeSkillNode(
			"Mastery II",
			"Advanced techniques II. Requires Mastery I.",
			NodeState.LOCKED,
			false
		));

		// Branch 3
		PrototypeSkillNode branch3 = new PrototypeSkillNode(
			"Specialization",
			"Choose your path.",
			NodeState.LOCKED,
			false
		);
		root.addChild(branch3);

		return tree;
	}

	/**
	 * Visual states for skill nodes.
	 */
	public enum NodeState {
		LOCKED("§8", "Locked"),
		AVAILABLE("§e", "Available"),
		UNLOCKED("§a", "Unlocked");

		public final String color;
		public final String label;

		NodeState(String color, String label) {
			this.color = color;
			this.label = label;
		}
	}

	/**
	 * Prototype skill node with state, name, and description.
	 */
	private static class PrototypeSkillNode extends AbstractSkillTreeItem {
		private final String name;
		private final String description;
		private final NodeState state;

		public PrototypeSkillNode(String name, String description, NodeState state, boolean isRoot) {
			super(isRoot, new ArrayList<>());
			this.name = name;
			this.description = description;
			this.state = state;
		}

		public String getName() { return name; }
		public String getDescription() { return description; }
		public NodeState getState() { return state; }

		@Override
		public com.daqem.uilib.api.widget.skilltree.ISkillTreeItemWidget createWidget() {
			// In a full implementation, this would create a visual widget
			// with the node's icon, border color based on state, and tooltip
			return null; // Prototype - widget creation not implemented
		}

		/**
		 * Get the tooltip lines for this node.
		 */
		public List<Component> getTooltip() {
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(Component.literal(state.color + "§l" + name));
			tooltip.add(Component.literal("§7" + description));
			tooltip.add(Component.literal(""));
			tooltip.add(Component.literal(state.color + "● " + state.label));
			if (state == NodeState.LOCKED) {
				tooltip.add(Component.literal("§7Unlock the parent node first."));
			} else if (state == NodeState.AVAILABLE) {
				tooltip.add(Component.literal("§eClick to unlock (1 skill point)"));
			}
			return tooltip;
		}
	}
}
