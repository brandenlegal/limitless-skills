package com.branden.limitskills.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class LimitlessSkillsClient implements ClientModInitializer {
	public static KeyMapping OPEN_SKILLS;

	@Override
	public void onInitializeClient() {
		ClientSkillCache.init();

		OPEN_SKILLS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.limitskills.open",
			71, // GLFW_KEY_G
			"category.limitskills"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_SKILLS.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new SkillScreen());
				}
			}
		});
	}
}
