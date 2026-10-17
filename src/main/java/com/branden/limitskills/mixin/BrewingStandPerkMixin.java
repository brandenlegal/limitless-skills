package com.branden.limitskills.mixin;

import com.branden.limitskills.BrewingXpOwners;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandPerkMixin {
	@Inject(method = "doBrew", at = @At("TAIL"))
	private static void limitskills$awardBrewXp(ServerLevel level, BlockPos pos, BrewingStandBlockEntity stand, CallbackInfo ci) {
		BrewingXpOwners.onBrew(level, pos);
	}
}
