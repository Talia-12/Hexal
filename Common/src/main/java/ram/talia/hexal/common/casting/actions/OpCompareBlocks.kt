package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3

object OpCompareBlocks : ConstManaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val blockPos0 = args.getBlockPos(0, argc)
        val blockPos1 = args.getBlockPos(1, argc)

        if (!ctx.isVecInRange(Vec3.atCenterOf(blockPos0)) || !ctx.isVecInRange(Vec3.atCenterOf(blockPos1)))
            return null.asActionResult

        val blockState0 = ctx.world.getBlockState(blockPos0)
        val blockState1 = ctx.world.getBlockState(blockPos1)

        return blockState0.block.equals(blockState1.block).asActionResult
    }
}