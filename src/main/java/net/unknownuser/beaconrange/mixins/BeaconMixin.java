package net.unknownuser.beaconrange.mixins;

import net.minecraft.block.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BeaconBlockEntity.class)
public class BeaconMixin {
	@Inject(at = @At("HEAD"), method = "applyPlayerEffects", cancellable = true)
	private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, StatusEffect primaryEffect, StatusEffect secondaryEffect, CallbackInfo ci) {
		// replace method entirely
		ci.cancel();
		
		if (!world.isClient && primaryEffect != null) {
			double range           = (beaconLevel * 100);
			int    effectAmplifier = 0;
			if (beaconLevel >= 4 && primaryEffect == secondaryEffect) {
				effectAmplifier = 1;
			}
			
			int                    effectDuration = (9 + beaconLevel * 2) * 20;
			Box                    rangeBox       = new Box(pos).expand(range).stretch(0.0, world.getHeight(), 0.0);
			List<PlayerEntity>     players        = world.getNonSpectatingEntities(PlayerEntity.class, rangeBox);
			Iterator<PlayerEntity> playerIterator = players.iterator();
			
			PlayerEntity playerEntity;
			while (playerIterator.hasNext()) {
				playerEntity = playerIterator.next();
				playerEntity.addStatusEffect(new StatusEffectInstance(primaryEffect, effectDuration, effectAmplifier, true, true));
			}
			
			if (beaconLevel >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null) {
				playerIterator = players.iterator();
				
				while (playerIterator.hasNext()) {
					playerEntity = playerIterator.next();
					playerEntity.addStatusEffect(new StatusEffectInstance(secondaryEffect, effectDuration, 0, true, true));
				}
			}
			
		}
	}
}
