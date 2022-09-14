package ram.talia.hexal.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.common.lib.HexalBlocks;

import java.util.List;
import java.util.Random;

@Mixin(GeodeFeature.class)
abstract public class MixinGeodeFeature {
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void isReplaceableSlipway (Codec codec, CallbackInfo ci) {
		HexalAPI.LOGGER.info("Creating MixinGeodeFeature");
	}
	
	/**
	 * Modifies {@link GeodeFeature#place(FeaturePlaceContext)} to make it occasionally place a Slipway in the centre of the geode.
	 */
	@Inject(method = "place", at = @At(
						value = "RETURN",
						ordinal = 1),
					locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					remap = false
	)
	private void placeSlipway (FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
		
		HexalAPI.LOGGER.info("placing slipway at " + origin);
		HexalAPI.LOGGER.info("numbers are $$16: " + $$16 + ", $$17: " + $$17 + ", $$18: " + $$18 + ", $$19: " + $$19);
		HexalAPI.LOGGER.info("minGenOffset: " + $$5 + ", maxGenOffset: " + $$6);
		HexalAPI.LOGGER.info("this might be important: " + $$7);
		worldGenLevel.setBlock(origin, HexalBlocks.SLIPWAY.defaultBlockState(), 2);
	}
}
