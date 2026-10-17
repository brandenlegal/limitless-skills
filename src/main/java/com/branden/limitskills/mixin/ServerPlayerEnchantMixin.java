package com.branden.limitskills.mixin;

import com.branden.limitskills.XpHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Awards Arcane XP for levels actually spent on an enchantment. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEnchantMixin {
	@Inject(method = "onEnchantmentPerformed", at = @At("TAIL"))
	private void limitskills$onEnchant(ItemStack stack, int levels, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (levels > 0 && !player.hasInfiniteMaterials()) {
			XpHooks.onEnchant(player, levels);
		}
	}
}
