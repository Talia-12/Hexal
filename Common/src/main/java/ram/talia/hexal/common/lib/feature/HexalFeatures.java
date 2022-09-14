package ram.talia.hexal.common.lib.feature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.common.levelgen.feature.SlipwayGeodeConfiguration;
import ram.talia.hexal.common.levelgen.feature.SlipwayGeodeFeature;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class HexalFeatures {
	public static void registerFeatures (BiConsumer<Feature<?>, ResourceLocation> r) {
		HexalAPI.LOGGER.info("registering hexal features");
		for (var e : FEATURES.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}
	
	private static final Map<ResourceLocation, Feature<?>> FEATURES = new LinkedHashMap<>();
	
	public static final Feature<SlipwayGeodeConfiguration> SLIPWAY_GEODE = register("slipway_geode", new SlipwayGeodeFeature(SlipwayGeodeConfiguration.CODEC));
	
	private static <T extends FeatureConfiguration> Feature<T> register(String id, Feature<T> feature) {
		var old = FEATURES.put(modLoc(id), feature);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return feature;
	}
}
