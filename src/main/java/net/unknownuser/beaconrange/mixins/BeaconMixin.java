package net.unknownuser.beaconrange.mixins;

import net.minecraft.core.*;
import net.minecraft.tags.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.unknownuser.beaconrange.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconMixin extends BlockEntity {
	// random constructor, because it needs to be here
	protected BeaconMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
		super(type, worldPosition, blockState);
	}
	
	@Unique
	private static final Map<Level, Map<BlockPos, Double>> dimensionMapping = new HashMap<>();
	
	@ModifyVariable(method = "applyEffects", at = @At("STORE"), name = "range")
	private static double applyNewBeaconRange(
		double range,
		final Level level,
		final BlockPos worldPosition,
		final int levels,
		final Holder<MobEffect> primaryPower,
		final Holder<MobEffect> secondaryPower
	) {
		return (levels * Config.rangePerLevel() * dimensionMapping.getOrDefault(level, new HashMap<>())
			.getOrDefault(worldPosition, 1.0)) + Config.baseOffset();
	}
	
	@Inject(at = @At("HEAD"), method = "updateBase", cancellable = true)
	private static void updateLevel(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir) { // NOSONAR
		// replace entire method
		cir.cancel();
		
		int currentDepth = 0;
		
		double multiplier = 1;
		
		for (int depth = 1; depth <= 4; currentDepth = depth++) { // NOSONAR
			int k = y - depth;
			if (k < level.getMinY()) {
				break;
			}
			
			boolean isOK = true;
			
			boolean levelIsOnlySameBlock = true;
			Block   block                = null;
			
			for (int l = x - depth; l <= x + depth && isOK; ++l) {
				for (int m = z - depth; m <= z + depth; ++m) {
					BlockState blockState = level.getBlockState(new BlockPos(l, k, m));
					
					if (!blockState.is(BlockTags.BEACON_BASE_BLOCKS)) {
						isOK = false;
						break;
					}
					
					if (block == null) {
						block = blockState.getBlock();
					} else if (levelIsOnlySameBlock && block != blockState.getBlock()) {
						levelIsOnlySameBlock = false;
					}
				}
			}
			
			if (levelIsOnlySameBlock) {
				multiplier *= Config.blockMultiplier(block);
			}
			
			if (!isOK) {
				break;
			}
		}
		
		dimensionMapping.computeIfAbsent(level, _ -> new HashMap<>()).put(new BlockPos(x, y, z), multiplier);
		
		cir.setReturnValue(currentDepth);
	}
	
	@Inject(at = @At("HEAD"), method = "setRemoved")
	private void setRemoved(CallbackInfo ci) {
		dimensionMapping.computeIfPresent(
			level, (_, multiplierMapping) -> {
				multiplierMapping.remove(this.worldPosition);
				return multiplierMapping.isEmpty() ? null : multiplierMapping;
			}
		);
	}
}
