package ram.talia.hexal.forge.eventhandlers;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.common.lib.feature.HexalPlacedFeatures;

public class BiomeGenerationEventHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void biomeLoading(final BiomeLoadingEvent event) {
		HexalAPI.LOGGER.info("loading a biome " + event);
		
		HexalPlacedFeatures.placeGeodesInBiome((feature, decoration) -> event.getGeneration().addFeature(decoration, Holder.direct(feature)));
		
		HexalAPI.LOGGER.info("biome now has a feature list of " + event.getGeneration().getFeatures(GenerationStep.Decoration.UNDERGROUND_DECORATION));
	}
}
