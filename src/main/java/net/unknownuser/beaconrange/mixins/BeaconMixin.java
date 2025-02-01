package net.unknownuser.beaconrange.mixins;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.*;
import net.minecraft.registry.tag.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.unknownuser.beaconrange.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BeaconBlockEntity.class)
public class BeaconMixin {
	private BeaconMixin() {}
	
	@Unique
	private static double baseBlockFactor = 1;
	
	@Inject(at = @At("HEAD"), method = "applyPlayerEffects", cancellable = true)
	private static void applyPlayerEffects(
		World world,
		BlockPos pos,
		int beaconLevel,
		RegistryEntry<StatusEffect> primaryEffect,
		RegistryEntry<StatusEffect> secondaryEffect,
		CallbackInfo ci
	) {
		// This method is nearly entirely copied from the default implementation, except the range declaration line
		
		// replace method entirely
		ci.cancel();
		
		if (!world.isClient && primaryEffect != null) {
			double range           = (beaconLevel * Config.rangePerLevel() * baseBlockFactor) + Config.baseOffset(); // CHANGES MADE HERE
			int    effectAmplifier = 0;
			
			if (beaconLevel >= 4 && Objects.equals(primaryEffect, secondaryEffect)) {
				effectAmplifier = 1;
			}
			
			int                    effectDuration = (9 + beaconLevel * 2) * 20;
			Box                    rangeBox       = new Box(pos).expand(range).stretch(0.0, world.getHeight(), 0.0);
			List<PlayerEntity>     players        = world.getNonSpectatingEntities(PlayerEntity.class, rangeBox);
			Iterator<PlayerEntity> playerIterator = players.iterator();
			
			PlayerEntity playerEntity;
			while (playerIterator.hasNext()) {
				playerEntity = playerIterator.next();
				playerEntity.addStatusEffect(new StatusEffectInstance(
					primaryEffect,
					effectDuration,
					effectAmplifier,
					true,
					true
				));
			}
			
			if (beaconLevel >= 4 && !Objects.equals(primaryEffect, secondaryEffect) && secondaryEffect != null) {
				playerIterator = players.iterator();
				
				while (playerIterator.hasNext()) {
					playerEntity = playerIterator.next();
					playerEntity.addStatusEffect(new StatusEffectInstance(
						secondaryEffect,
						effectDuration,
						0,
						true,
						true
					));
				}
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "updateLevel", cancellable = true)
	private static void updateLevel(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) { // NOSONAR
		// same with above, this method is copied a lot, however there are more changes
		
		// replace entire method
		cir.cancel();
		
		int i = 0;
		
		double multiplier = 1;
		
		for (int j = 1; j <= 4; i = j++) { // NOSONAR
			int k = y - j;
			if (k < world.getBottomY()) {
				break;
			}
			
			boolean bl = true;
			
			boolean    levelIsOnlySameBlock = true;
			Identifier identifier           = null;
			
			for (int l = x - j; l <= x + j && bl; ++l) {
				for (int m = z - j; m <= z + j; ++m) {
					BlockState blockState = world.getBlockState(new BlockPos(l, k, m));
					
					if (!blockState.isIn(BlockTags.BEACON_BASE_BLOCKS)) {
						bl = false;
						break;
					}
					
					if (identifier == null) {
						identifier = Registries.BLOCK.getId(blockState.getBlock());
					} else if (levelIsOnlySameBlock && !identifier.equals(Registries.BLOCK.getId(blockState.getBlock()))) {
						levelIsOnlySameBlock = false;
					}
				}
			}
			
			if (levelIsOnlySameBlock) {
				multiplier *= Config.blockMultiplier(identifier);
			}
			
			if (!bl) {
				break;
			}
		}
		
		baseBlockFactor = multiplier;
		
		cir.setReturnValue(i);
	}
}
