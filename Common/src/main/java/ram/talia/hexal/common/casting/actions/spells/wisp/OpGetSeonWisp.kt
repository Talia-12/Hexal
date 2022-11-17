package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.xplat.IXplatAbstractions

object OpGetSeonWisp : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return IXplatAbstractions.INSTANCE.getSeon(ctx.caster)?.asSpellResult ?: listOf(SpellDatum.make(Widget.NULL))
    }
}