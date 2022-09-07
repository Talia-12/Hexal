package ram.talia.hexal.common.blocks

import at.petrak.hexcasting.xplat.IForgeLikeBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway

class BlockSlipway(properties: Properties) : Block(properties), EntityBlock, IForgeLikeBlock {

	override fun newBlockEntity(pPos: BlockPos, pState: BlockState) = BlockEntitySlipway(pPos, pState)

	override fun <T : BlockEntity> getTicker(pLevel: Level, pState: BlockState?, pBlockEntityType: BlockEntityType<T>?): BlockEntityTicker<T> {
		return BlockEntityTicker { level: Level, blockPos: BlockPos, blockState: BlockState, t: T ->
				tick(level, blockPos, blockState, t)
		}
	}

	companion object {
		private fun <T : BlockEntity> tick(level: Level, blockPos: BlockPos, blockState: BlockState, t: T) {
			if (t is BlockEntitySlipway) {
				t.tick();
			}
		}
	}
}