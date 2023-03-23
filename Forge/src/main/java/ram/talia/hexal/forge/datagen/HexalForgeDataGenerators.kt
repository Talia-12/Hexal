package ram.talia.hexal.forge.datagen

import at.petrak.paucal.api.forge.datagen.PaucalForgeDatagenWrappers
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.lib.HexalSounds
import ram.talia.hexal.datagen.HexalBlockTagProvider
import ram.talia.hexal.datagen.HexalLootTables
import ram.talia.hexal.datagen.recipes.HexalplatRecipes

class HexalForgeDataGenerators {
	companion object {
		@JvmStatic
		@SubscribeEvent
		fun generateData(ev: GatherDataEvent) {
			if (System.getProperty("hexal.xplat_datagen") != null) {
				configureXplatDatagen(ev)
			}
			if (System.getProperty("hexal.forge_datagen") != null) {
				configureForgeDatagen(ev)
			}
		}

		private fun configureXplatDatagen(ev: GatherDataEvent) {
			HexalAPI.LOGGER.info("Starting cross-platform datagen")

			val gen = ev.generator
//			val efh = ev.existingFileHelper
			gen.addProvider(ev.includeClient(), HexalSounds.provider(gen))
//			gen.addProvider(ev.includeClient(), HexItemModels(gen, efh))
//			gen.addProvider(ev.includeClient(), HexBlockStatesAndModels(gen, efh))
//			gen.addProvider(ev.includeServer(), PaucalForgeDatagenWrappers.addEFHToAdvancements(HexAdvancements(gen), efh))
		}

		private fun configureForgeDatagen(ev: GatherDataEvent) {
			HexalAPI.LOGGER.info("Starting Forge-specific datagen")

			val gen = ev.generator
			val efh = ev.existingFileHelper
			gen.addProvider(ev.includeServer(), HexalLootTables(gen))
			gen.addProvider(ev.includeServer(), HexalplatRecipes(gen))
			val blockTagProvider = PaucalForgeDatagenWrappers.addEFHToTagProvider(HexalBlockTagProvider(gen), efh)
			gen.addProvider(ev.includeServer(), blockTagProvider)
//				val itemTagProvider = PaucalForgeDatagenWrappers.addEFHToTagProvider(
//					HexItemTagProvider(gen, blockTagProvider, IXplatAbstractions.INSTANCE.tags()), efh
//				)
//				gen.addProvider(itemTagProvider)
		}
	}
}