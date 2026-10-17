package com.branden.limitskills.mixin;

import com.branden.limitskills.Perks;
import com.branden.limitskills.SkillManager;
import com.branden.limitskills.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerSkillPerksMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void limitskills$miningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			int level = SkillManager.get(player).getLevel(SkillType.MINING);
			cir.setReturnValue(cir.getReturnValue() * (float) (1.0 + Perks.miningSpeedBonus(level)));
		}
	}

	@org.spongepowered.asm.mixin.injection.ModifyVariable(method = "onEnchantmentPerformed", at = @At("HEAD"), argsOnly = true)
	private int limitskills$discountEnchantmentCost(int levels) {
		if ((Object) this instanceof ServerPlayer player) {
			int arcane = SkillManager.get(player).getLevel(SkillType.ARCANE);
			return Perks.discountedEnchantmentLevels(arcane, levels);
		}
		return levels;
	}
}
