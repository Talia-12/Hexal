package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import net.minecraft.world.phys.Vec3

object OpCompareBlocks : ConstManaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val blockPos0 = args.getBlockPos(0, argc)
        val blockPos1 = args.getBlockPos(1, argc)

        val blockState0 = ctx.world.getBlockState(blockPos0)
        val blockState1 = ctx.world.getBlockState(blockPos1)

        return if (ctx.isVecInRange(Vec3.atCenterOf(blockPos0)) && ctx.isVecInRange(Vec3.atCenterOf(blockPos1))) {
            blockState0.block.equals(blockState1.block).asActionResult
        } else {
            listOf(NullIota())
        }
    }
}