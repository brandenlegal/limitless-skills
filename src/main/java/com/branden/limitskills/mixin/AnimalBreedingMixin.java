package com.branden.limitskills.mixin;

import com.branden.limitskills.XpHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Credits the same breeder selected by vanilla for its advancement. */
@Mixin(Animal.class)
public abstract class AnimalBreedingMixin {
	@Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
	private void limitskills$onBred(ServerLevel level, Animal partner, AgeableMob child, CallbackInfo ci) {
		Animal parent = (Animal) (Object) this;
		ServerPlayer breeder = parent.getLoveCause();
		if (breeder == null) breeder = partner.getLoveCause();
		if (breeder != null) XpHooks.onAnimalBred(breeder);
	}
}
