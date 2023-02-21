package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.getBlockPosOrItem

object OpTypeBlockItem : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val blockPos = args.getBlockPos(0, argc)

        return args.getBlockPosOrItem(0, argc).map({
            if (!ctx.isVecInRange(Vec3.atCenterOf(blockPos)))
                null.asActionResult
            else {
                val blockState = ctx.world.getBlockState(blockPos)
                blockState.block.asActionResult
            }
        }, {
            it.item.asActionResult
        })
    }
}