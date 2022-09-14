package ram.talia.hexal.common.lib.feature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.common.levelgen.feature.SlipwayGeodeConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class HexalConfiguredFeatures {
	
	public static void registerConfiguredFeatures (BiConsumer<ConfiguredFeature<?, ?>, ResourceLocation> r) {
		HexalAPI.LOGGER.info("registering hexal configured features");
		for (var e : CONFIGURED_FEATURES.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}
	
	private static final Map<ResourceLocation, ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = new LinkedHashMap<>();
	
	public static final ConfiguredFeature<?, ?> AMETHYST_SLIPWAY_GEODE = register("amethyst_slipway_geode",
			new ConfiguredFeature<>(HexalFeatures.SLIPWAY_GEODE, new SlipwayGeodeConfiguration(
					new GeodeBlockSettings(
									BlockStateProvider.simple(Blocks.AIR),
									BlockStateProvider.simple(Blocks.AMETHYST_BLOCK),
									BlockStateProvider.simple(Blocks.BUDDING_AMETHYST),
									BlockStateProvider.simple(Blocks.CALCITE),
									BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
									List.of(
													Blocks.SMALL_AMETHYST_BUD.defaultBlockState(),
													Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState(),
													Blocks.LARGE_AMETHYST_BUD.defaultBlockState(),
													Blocks.AMETHYST_CLUSTER.defaultBlockState()
									), BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS
					),
					new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
					new GeodeCrackSettings(0.95, 2.0, 2),
					0.35,
					0.083,
					true,
					UniformInt.of(4, 6),
					UniformInt.of(3, 4),
					UniformInt.of(1, 2),
					-16,
					16,
					0.05,
					1
			))
	);
	
	private static <FC extends FeatureConfiguration, F extends Feature<FC>> ConfiguredFeature<FC, F> register(String id, ConfiguredFeature<FC, F> configuredFeature) {
		var old = CONFIGURED_FEATURES.put(modLoc(id), configuredFeature);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return configuredFeature;
	}
}
