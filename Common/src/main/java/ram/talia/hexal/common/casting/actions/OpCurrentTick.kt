package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpCurrentTick : ConstManaOperator {
	override val argc = 0

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		return (ctx.world.gameTime).asSpellResult
	}
}