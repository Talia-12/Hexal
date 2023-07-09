package ram.talia.hexal.common.blocks

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IForgeLikeBlock
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
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
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import kotlin.math.min

@Suppress("OVERRIDE_DEPRECATION")
class BlockRelay(properties: Properties) : Block(properties), EntityBlock, IForgeLikeBlock {
    init {
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
        )
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = BlockEntityRelay(pos, state)

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T> {
        return BlockEntityTicker(Companion::tick)
    }

    override fun getRenderShape(state: BlockState) = RenderShape.ENTITYBLOCK_ANIMATED

    override fun getShape(blockState: BlockState, level: BlockGetter, pos: BlockPos, ctx: CollisionContext): VoxelShape {
        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        return when (blockState.getValue(FACING)) {
            Direction.UP -> box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0)
            Direction.DOWN -> box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0)
            Direction.NORTH -> box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0)
            Direction.SOUTH -> box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0)
            Direction.WEST ->box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0)
            Direction.EAST -> box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0)
        }
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (state.block !is BlockRelay)
            return InteractionResult.PASS
        val relay = level.getBlockEntity(pos) as? BlockEntityRelay ?: return InteractionResult.PASS

        val stack = player.getItemInHand(hand).copy()
        if (!IXplatAbstractions.INSTANCE.isPigment(stack)) {
            relay.debug()
            return InteractionResult.PASS
        }

        if (removeItem(player, stack, 1)) {
            relay.setPigment(FrozenPigment(stack, player.uuid), level)
            return InteractionResult.SUCCESS
        }

        return InteractionResult.FAIL
    }

    @Suppress("DEPRECATION")
    override fun onRemove(blockState: BlockState, level: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!newState.`is`(this)) {
            val blockEntity = level.getBlockEntity(pos)

            if (blockEntity is BlockEntityRelay) {
                if (level is ServerLevel && !moved)
                    blockEntity.disconnectAll()
            }
        }

        super.onRemove(blockState, level, pos, newState, moved)
    }

    private fun removeItem(player: Player, item: ItemStack, count: Int): Boolean {
        val operativeItem = item.copy()

        val inv = player.inventory
        val stacksToExamine = inv.items.toMutableList().apply { removeAt(inv.selected) }.asReversed().toMutableList().apply {
            addAll(inv.offhand)
            add(inv.getSelected())
        }

        fun matches(stack: ItemStack): Boolean =
                !stack.isEmpty && ItemStack.isSameItemSameTags(operativeItem, stack)

        val presentCount = stacksToExamine.fold(0) { acc, stack ->
            acc + if (matches(stack)) stack.count else 0
        }
        if (presentCount < count) return false

        var remaining = count
        for (stack in stacksToExamine) {
            if (matches(stack)) {
                val toWithdraw = min(stack.count, remaining)
                stack.shrink(toWithdraw)

                remaining -= toWithdraw
                if (remaining <= 0) {
                    return true
                }
            }
        }
        throw RuntimeException("unreachable")
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(FACING, context.clickedFace)
    }

    companion object {
        val FACING: DirectionProperty = DirectionProperty.create("facing")

        @Suppress("UNUSED_PARAMETER")
        private fun <T : BlockEntity> tick(level: Level, pos: BlockPos, state: BlockState, t: T) {
            if (t is BlockEntityRelay) {
                if (level.isClientSide)
                    t.clientTick()
                else
                    t.serverTick()
            }
        }
    }
}