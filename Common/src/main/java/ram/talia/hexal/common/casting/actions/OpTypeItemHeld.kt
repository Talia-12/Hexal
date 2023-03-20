package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.item.Items
import ram.talia.hexal.api.asActionResult

object OpTypeItemHeld : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return ctx.getHeldItemToOperateOn { it.item != Items.AIR }.first.item.takeUnless { it == Items.AIR }?.asActionResult ?: null.asActionResult
    }
}