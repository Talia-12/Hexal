package ram.talia.hexal.forge.datagen

import at.petrak.hexcasting.forge.datagen.TagsProviderEFHSetter
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.lib.HexalSounds
import ram.talia.hexal.datagen.HexalBlockTagProvider
import ram.talia.hexal.datagen.HexalLootTables
import ram.talia.hexal.datagen.recipes.HexalplatRecipes
import ram.talia.hexal.datagen.tag.HexalActionTagProvider

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
			val output = gen.packOutput
//			val lookup = ev.lookupProvider
//			val efh = ev.existingFileHelper
			gen.addProvider(ev.includeClient(), HexalSounds.provider(output))
//			gen.addProvider(ev.includeClient(), HexItemModels(gen, efh))
//			gen.addProvider(ev.includeClient(), HexBlockStatesAndModels(gen, efh))
//			gen.addProvider(ev.includeServer(), PaucalForgeDatagenWrappers.addEFHToAdvancements(HexAdvancements(gen), efh))
		}

		private fun configureForgeDatagen(ev: GatherDataEvent) {
			HexalAPI.LOGGER.info("Starting Forge-specific datagen")

			val gen = ev.generator
			val output = gen.packOutput
			val lookup = ev.lookupProvider
			val efh = ev.existingFileHelper
			gen.addProvider(ev.includeServer(), LootTableProvider(
				output, setOf(), listOf(LootTableProvider.SubProviderEntry(::HexalLootTables, LootContextParamSets.ALL_PARAMS))
			))
			gen.addProvider(ev.includeServer(), HexalplatRecipes(output))
			val blockTagProvider = HexalBlockTagProvider(output, lookup)
			(blockTagProvider as TagsProviderEFHSetter).setEFH(efh)
			gen.addProvider(ev.includeServer(), blockTagProvider)
//				val itemTagProvider = PaucalForgeDatagenWrappers.addEFHToTagProvider(
//					HexItemTagProvider(gen, blockTagProvider, IXplatAbstractions.INSTANCE.tags()), efh
//				)
//				gen.addProvider(itemTagProvider)
			var hexTagProvider = HexalActionTagProvider(output, lookup)
			(hexTagProvider as TagsProviderEFHSetter).setEFH(efh)
			gen.addProvider(ev.includeServer(), hexTagProvider)
		}
	}
}