package com.branden.limitskills.mixin;

import com.branden.limitskills.AntiCheese;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Records successful server-side block placements for the existing XP guard. */
@Mixin(BlockItem.class)
public abstract class BlockItemPlacementMixin {
	@Inject(method = "placeBlock", at = @At("RETURN"))
	private void limitskills$onBlockPlaced(BlockPlaceContext context, BlockState state,
			CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && context.getPlayer() instanceof ServerPlayer player) {
			AntiCheese.onBlockPlaced(player, context.getClickedPos());
		}
	}
}
