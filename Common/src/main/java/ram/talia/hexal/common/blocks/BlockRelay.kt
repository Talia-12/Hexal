package ram.talia.hexal.common.blocks

import at.petrak.hexcasting.xplat.IForgeLikeBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay

class BlockRelay(properties: Properties) : Block(properties), EntityBlock, IForgeLikeBlock {
    override fun newBlockEntity(pos: BlockPos, state: BlockState) = BlockEntityRelay(pos, state)
}