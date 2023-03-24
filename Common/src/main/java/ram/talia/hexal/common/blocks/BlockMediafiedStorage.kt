package ram.talia.hexal.common.blocks

import at.petrak.hexcasting.xplat.IForgeLikeBlock
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
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
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage.AnimationState
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage.Companion.ANIMATION_LENGTH

/**
 * Block that actually stores all the mediafied items from all the players bound to it.
 * Exists so that it's possible to do *something* to prevent the mediafied items slowly
 * growing and growing in memory consumption.
 */
@Suppress("OVERRIDE_DEPRECATION")
class BlockMediafiedStorage(properties: Properties) : Block(properties), EntityBlock, IForgeLikeBlock {
    override fun newBlockEntity(pos: BlockPos, state: BlockState) = BlockEntityMediafiedStorage(pos, state)

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker(Companion::tick)
    }

    companion object {
        private fun <T : BlockEntity> tick(level: Level, blockPos: BlockPos, blockState: BlockState, t: T) {
            if (t is BlockEntityMediafiedStorage) {
                if (level.isClientSide)
                    t.clientTick()
                else
                    t.serverTick()
            }
        }

        val CLOSED_SHAPE = box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0)
        val OPEN_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    }

    override fun getShape(blockState: BlockState, level: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape {
        val animation = (level.getBlockEntity(pos) as? BlockEntityMediafiedStorage)?.currentAnimation ?: return OPEN_SHAPE
        return if (animation is AnimationState.Closing) {
            box(0.0, 0.0, 0.0, 16.0, 16.0 - 6.0 * animation.progress.toDouble() / ANIMATION_LENGTH, 16.0)
        } else {
            box(0.0, 0.0, 0.0, 16.0, 10.0 + 6.0 * animation.progress.toDouble() / ANIMATION_LENGTH, 16.0)
        }
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }

    override fun onRemove(blockState: BlockState, level: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!newState.`is`(this)) {
            val blockEntity = level.getBlockEntity(pos)

            if (blockEntity is BlockEntityMediafiedStorage) {
                if (level is ServerLevel && !moved)
                    blockEntity.dropAllContents(level, pos)

                MediafiedItemManager.removeStorage(blockEntity.uuid)
            }
        }

        super.onRemove(blockState, level, pos, newState, moved)
    }

    override fun getOcclusionShape(blockState: BlockState, level: BlockGetter, pos: BlockPos): VoxelShape {
        return Shapes.empty()
    }
}