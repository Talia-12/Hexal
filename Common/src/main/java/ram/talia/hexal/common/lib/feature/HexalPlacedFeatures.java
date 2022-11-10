package ram.talia.hexal.common.lib.feature;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class HexalPlacedFeatures {
	
	public static void registerPlacedFeatures(BiConsumer<PlacedFeature, ResourceLocation> r) {
		for (var e : PLACED_FEATURES.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}
	
	public static void placeGeodesInBiome (BiConsumer<PlacedFeature, GenerationStep.Decoration> p) {
		p.accept(AMETHYST_SLIPWAY_GEODE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
	}
	
	private static final Map<ResourceLocation, PlacedFeature> PLACED_FEATURES = new LinkedHashMap<>();
	
	public static final PlacedFeature AMETHYST_SLIPWAY_GEODE = register(
					"amethyst_slipway_geode",
					new PlacedFeature(Holder.direct(HexalConfiguredFeatures.AMETHYST_SLIPWAY_GEODE),
														List.of(
																		RarityFilter.onAverageOnceEvery(36),
																		InSquarePlacement.spread(),
																		HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(30)),
																		BiomeFilter.biome()
														)
					));
	
	private static PlacedFeature register (String id, PlacedFeature placedFeature) {
		var old = PLACED_FEATURES.put(modLoc(id), placedFeature);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return placedFeature;
	}
}
