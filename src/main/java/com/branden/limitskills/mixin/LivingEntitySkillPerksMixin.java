package com.branden.limitskills.mixin;

import com.branden.limitskills.Perks;
import com.branden.limitskills.SkillManager;
import com.branden.limitskills.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySkillPerksMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float limitskills$damagePerks(float amount, ServerLevel level, DamageSource source) {
		if ((Object) this instanceof ServerPlayer victim) {
			int survival = SkillManager.get(victim).getLevel(SkillType.SURVIVAL);
			amount *= (float) (1.0 - Perks.damageReduction(survival));
		}
		if (source.getEntity() instanceof ServerPlayer attacker) {
			int combat = SkillManager.get(attacker).getLevel(SkillType.COMBAT);
			amount *= (float) (1.0 + Perks.damageBonus(combat));
			if (Perks.rollCriticalHit(combat, attacker.getRandom())) amount *= 1.5f;
		}
		return amount;
	}

	@ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true)
	private MobEffectInstance limitskills$potionDuration(MobEffectInstance effect, MobEffectInstance original, Entity source) {
		if (source instanceof ServerPlayer player) {
			int arcane = SkillManager.get(player).getLevel(SkillType.ARCANE);
			return effect.withScaledDuration((float) (1.0 + Perks.potionDurationBonus(arcane)));
		}
		return effect;
	}
}
