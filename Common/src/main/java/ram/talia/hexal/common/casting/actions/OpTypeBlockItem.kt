package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.getBlockPosOrItemOrItemEntity

object OpTypeBlockItem : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return args.getBlockPosOrItemOrItemEntity(0, argc).flatMap({
            if (!ctx.isVecInRange(Vec3.atCenterOf(it)))
                null.asActionResult
            else {
                val blockState = ctx.world.getBlockState(it)
                blockState.block.asActionResult
            }
        }, {
            it?.item?.asActionResult ?: null.asActionResult
        }, {
            it.item.item.asActionResult
        })
    }
}