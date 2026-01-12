package net.unknownuser.beaconrange.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.unknownuser.beaconrange.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(BeaconBlockEntity.class)
public class BeaconMixin {
	private BeaconMixin() {}
	
	@Unique
	private static double baseBlockFactor = 1;

	@ModifyVariable(method = "applyPlayerEffects", at = @At("STORE"))
	private static double applyNewBeaconRange(double range, @Local(argsOnly = true) int beaconLevel) {
		return (beaconLevel * Config.rangePerLevel() * baseBlockFactor) + Config.baseOffset();
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
