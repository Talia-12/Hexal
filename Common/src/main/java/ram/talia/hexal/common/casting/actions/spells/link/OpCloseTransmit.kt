package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.xplat.IXplatAbstractions

object OpCloseTransmit : ConstManaOperator {
	override val argc = 0

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		IXplatAbstractions.INSTANCE.resetPlayerTransmittingTo(ctx.caster)
		return listOf()
	}
}