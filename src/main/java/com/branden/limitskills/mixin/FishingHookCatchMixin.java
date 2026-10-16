package com.branden.limitskills.mixin;

import com.branden.limitskills.XpHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uses vanilla's fish-caught stat point to exclude junk and hooked entities. */
@Mixin(FishingHook.class)
public abstract class FishingHookCatchMixin {
	@Inject(method = "retrieve", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/Identifier;I)V"))
	private void limitskills$onFishCaught(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
		if (((FishingHook) (Object) this).getPlayerOwner() instanceof ServerPlayer player) {
			XpHooks.onFishCaught(player);
		}
	}
}
