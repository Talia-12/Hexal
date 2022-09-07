package ram.talia.hexal.common.lib

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import com.mojang.datafixers.util.Pair
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.AbstractGlassBlock
import net.minecraft.world.level.block.AmethystBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.material.MaterialColor
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.blocks.BlockSlipway
import java.util.function.BiConsumer

class HexalBlocks {

	companion object {
		@JvmStatic
		public fun registerBlocks(r: BiConsumer<Block, ResourceLocation>) {
			for ((key, value) in HexalBlocks.BLOCKS) {
				r.accept(value!!, key!!)
			}
		}

		@JvmStatic
		fun registerBlockItems(r: BiConsumer<Item, ResourceLocation>) {
			for ((key, value) in BLOCK_ITEMS) {
				r.accept(BlockItem(value.first, value.second), key)
			}
		}

		private val BLOCKS: MutableMap<ResourceLocation, Block> = LinkedHashMap()
		private val BLOCK_ITEMS: MutableMap<ResourceLocation, Pair<Block, Item.Properties>> = java.util.LinkedHashMap()

		val SLIPWAY = blockNoItem("slipway", BlockSlipway(
			BlockBehaviour.Properties.of(Material.AIR, MaterialColor.NONE)
				.noDrops()
				.strength(-1.0f, 3600000.0f)
				.noCollission()
				.noOcclusion()
		))

		private fun <T : Block> blockNoItem(name: String, block: T): T {
			val old = BLOCKS.put(HexalAPI.modLoc(name), block)
			require(old == null) { "Typo? Duplicate id $name" }
			return block
		}

		private fun <T : Block> blockItem(name: String, block: T): T {
			return blockItem(name, block, HexItems.props())
		}

		private fun <T : Block> blockItem(name: String, block: T, props: Item.Properties): T {
			blockNoItem(name, block)
			val old = BLOCK_ITEMS.put(HexalAPI.modLoc(name), Pair(block, props))
			require(old == null) { "Typo? Duplicate id $name" }
			return block
		}
	}
}