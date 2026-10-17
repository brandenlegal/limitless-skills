package com.branden.limitskills.mixin;

import com.branden.limitskills.SkillStore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps skill data available through vanilla's final player save. */
@Mixin(PlayerList.class)
public abstract class PlayerListSaveCleanupMixin {
	@Inject(method = "remove", at = @At("TAIL"))
	private void limitskills$afterPlayerSaved(ServerPlayer player, CallbackInfo ci) {
		SkillStore.onPlayerDisconnect(player.getUUID());
	}
}
