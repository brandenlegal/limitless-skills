package com.branden.limitskills.mixin;

import com.branden.limitskills.SkillData;
import com.branden.limitskills.SkillStore;
import com.branden.limitskills.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persists Limitless Skills data to the player's NBT.
 * Hooks into vanilla save/load — the standard approach used by all major mods.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSkillDataMixin {

	private static final String NBT_KEY = "limitskills";

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void limitskills$saveSkillData(ValueOutput output, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		SkillData data = SkillStore.getCached(player.getUUID());
		if (data == null) return;

		// Store each skill's XP and level
		for (SkillType type : SkillType.values()) {
			output.putDouble(NBT_KEY + "_xp_" + type.id, data.getXp(type));
			output.putInt(NBT_KEY + "_level_" + type.id, data.getLevel(type));
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void limitskills$loadSkillData(ValueInput input, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		SkillData data = new SkillData();

		// Bulk load via arrays
		double[] xpArr = new double[SkillType.values().length];
		int[] lvlArr = new int[SkillType.values().length];
		for (SkillType type : SkillType.values()) {
			int i = type.ordinal();
			xpArr[i] = input.getDoubleOr(NBT_KEY + "_xp_" + type.id, 0.0);
			lvlArr[i] = input.getIntOr(NBT_KEY + "_level_" + type.id, 0);
		}
		data.fromArrays(xpArr, lvlArr);

		SkillStore.putCached(player.getUUID(), data);
	}
}
