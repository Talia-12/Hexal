package ram.talia.hexal.common.lib

import at.petrak.hexcasting.common.lib.HexItems
import com.mojang.datafixers.util.Pair
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.material.MaterialColor
import net.minecraft.world.level.material.PushReaction
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.common.blocks.BlockMediafiedStorage
import ram.talia.hexal.common.blocks.BlockRelay
import ram.talia.hexal.common.blocks.BlockSlipway
import java.util.function.BiConsumer

class HexalBlocks {

	companion object {
		@JvmStatic
		fun registerBlocks(r: BiConsumer<Block, ResourceLocation>) {
			for ((key, value) in BLOCKS) {
				r.accept(value, key)
			}
		}

		@JvmStatic
		fun registerBlockItems(r: BiConsumer<Item, ResourceLocation>) {
			for ((key, value) in BLOCK_ITEMS) {
				r.accept(BlockItem(value.first, value.second), key)
			}
		}

        private val BLOCKS: MutableMap<ResourceLocation, Block> = LinkedHashMap()
		private val BLOCK_ITEMS: MutableMap<ResourceLocation, Pair<Block, Item.Properties>> = LinkedHashMap()

		@JvmField
		val SLIPWAY = blockNoItem("slipway", BlockSlipway(
			//Material.Builder.notSolidBlocking is for some unimaginable reason package-private, so we're doing this instead
			// setting the slipway as blocksMotion even though it doesn't so that fluids can't replace it.
			BlockBehaviour.Properties.of(Material(MaterialColor.NONE, false, false, true, false, false, false, PushReaction.BLOCK))
				.noLootTable()
				.strength(-1.0f, 3600000.0f)
				.noCollission()
				.noOcclusion()
		))

		@JvmField
		val MEDIAFIED_STORAGE = blockItem("mediafied_storage", BlockMediafiedStorage(
				BlockBehaviour.Properties.of(Material.AMETHYST).noOcclusion().strength(30.0f)
		))

		val RELAY = blockNoItem("relay", BlockRelay(
				BlockBehaviour.Properties.of(Material.AMETHYST).noOcclusion().strength(3.0f)
		))


		private fun <T : Block> blockNoItem(name: String, block: T): T {
			val old = BLOCKS.put(modLoc(name), block)
			require(old == null) { "Typo? Duplicate id $name" }
			return block
		}

		private fun <T : Block> blockItem(name: String, block: T): T {
			return blockItem(name, block, HexItems.props())
		}

		private fun <T : Block> blockItem(name: String, block: T, props: Item.Properties): T {
			blockNoItem(name, block)
			val old = BLOCK_ITEMS.put(modLoc(name), Pair(block, props))
			require(old == null) { "Typo? Duplicate id $name" }
			return block
		}
	}
}