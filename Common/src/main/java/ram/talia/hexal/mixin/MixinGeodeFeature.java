package ram.talia.hexal.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.common.lib.HexalBlocks;

import java.util.List;
import java.util.Random;

@Mixin(GeodeFeature.class)
abstract public class MixinGeodeFeature {
	
	/**
	 * Modifies {@link GeodeFeature#place(FeaturePlaceContext)} to make it occasionally place a Slipway in the centre of the geode.
	 */
	@Inject(method = "place", at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/levelgen/feature/GeodeFeature;isReplaceable(Lnet/minecraft/tags/TagKey;)Ljava/util/function/Predicate;",
					shift = At.Shift.AFTER),
					locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private void placeSlipway (FeaturePlaceContext<GeodeConfiguration> $$0, CallbackInfoReturnable<Boolean> cir, GeodeConfiguration $$1, Random $$2, BlockPos origin,
														 WorldGenLevel worldGenLevel, int $$5, int $$6, List $$7, int $$8, WorldgenRandom worldgenRandom, NormalNoise $$10, List $$11, double $$12,
														 GeodeLayerSettings $$13, GeodeBlockSettings $$14, GeodeCrackSettings $$15, double $$16, double $$17, double $$18, double $$19, double $$20,
														 boolean $$21, int $$22, List $$31) {
		HexalAPI.LOGGER.info("haha");
		worldGenLevel.setBlock(origin, HexalBlocks.SLIPWAY.defaultBlockState(), 2);
	}
}
