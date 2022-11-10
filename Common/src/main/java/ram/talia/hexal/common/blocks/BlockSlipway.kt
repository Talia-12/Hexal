package ram.talia.hexal.common.blocks

import at.petrak.hexcasting.xplat.IForgeLikeBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway

class BlockSlipway(properties: Properties) : Block(properties), EntityBlock, IForgeLikeBlock {

	override fun newBlockEntity(pPos: BlockPos, pState: BlockState) = BlockEntitySlipway(pPos, pState)

	override fun <T : BlockEntity> getTicker(pLevel: Level, pState: BlockState, pBlockEntityType: BlockEntityType<T>): BlockEntityTicker<T> {
		return BlockEntityTicker(Companion::tick)
	}

	@Deprecated("Deprecated in Java", ReplaceWith("RenderShape.ENTITYBLOCK_ANIMATED", "net.minecraft.world.level.block.RenderShape"))
	override fun getRenderShape(state: BlockState): RenderShape {
		return RenderShape.ENTITYBLOCK_ANIMATED
	}

	@Deprecated("Deprecated in Java", ReplaceWith("Shapes.empty()", "net.minecraft.world.phys.shapes.Shapes"))
	override fun getShape(state: BlockState, worldIn: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
		return Shapes.empty()
	}

	companion object {
		private fun <T : BlockEntity> tick(level: Level, blockPos: BlockPos, blockState: BlockState, t: T) {
			if (t is BlockEntitySlipway) {
				t.tick()
			}
		}
	}
}