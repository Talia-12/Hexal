package ram.talia.hexal.common.lib

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.common.lib.HexBlockEntities
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway
import java.util.function.BiConsumer
import java.util.function.BiFunction

class HexalBlockEntities {
	companion object {
		@JvmStatic
		fun registerBlockEntities(r: BiConsumer<BlockEntityType<*>, ResourceLocation>) {
			for ((key, value) in BLOCK_ENTITIES) {
				r.accept(value, key)
			}
		}

		private val BLOCK_ENTITIES: MutableMap<ResourceLocation, BlockEntityType<*>> = LinkedHashMap()

		@JvmField
		public val SLIPWAY = register("slipway", ::BlockEntitySlipway, HexalBlocks.SLIPWAY)

		private fun <T : BlockEntity?> register(
			id: String,
			func: BiFunction<BlockPos, BlockState, T>, vararg blocks: Block
		): BlockEntityType<T>? {
			val ret = IXplatAbstractions.INSTANCE.createBlockEntityType(func, *blocks)
			val old = BLOCK_ENTITIES[modLoc(id)]
			require(old == null) { "Duplicate id $id" }
			BLOCK_ENTITIES[modLoc(id)] = ret
			return ret
		}
	}
}