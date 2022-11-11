package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.asActionResult

object OpTypeBlock : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val blockPos = args.getBlockPos(0, argc)

        if (!ctx.isVecInRange(Vec3.atCenterOf(blockPos)))
            return null.asActionResult

        val blockState = ctx.world.getBlockState(blockPos)

        return blockState.block.asActionResult
    }
}