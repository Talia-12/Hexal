package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object OpCompareBlocks : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val vec0 = args.getChecked<Vec3>(0, argc)
        val vec1 = args.getChecked<Vec3>(1, argc)

        val blockPos0 = BlockPos(vec0)
        val blockPos1 = BlockPos(vec1)

        if (!ctx.isVecInRange(Vec3.atCenterOf(blockPos0)) || !ctx.isVecInRange(Vec3.atCenterOf(blockPos1)))
            return null.asSpellResult

        val blockState0 = ctx.world.getBlockState(blockPos0)
        val blockState1 = ctx.world.getBlockState(blockPos1)

        return blockState0.block.equals(blockState1.block).asSpellResult
    }
}