package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import ram.talia.hexal.xplat.IXplatAbstractions

object OpSeonWispGet : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return IXplatAbstractions.INSTANCE.getSeon(ctx.caster)?.asActionResult ?: listOf(NullIota())
    }
}