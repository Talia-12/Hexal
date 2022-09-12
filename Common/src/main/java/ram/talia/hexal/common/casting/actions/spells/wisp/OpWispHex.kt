package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import ram.talia.hexal.common.entities.BaseCastingWisp

object OpWispHex : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val wisp = args.getChecked<BaseCastingWisp>(0, argc)

		if (wisp.caster != ctx.caster)
			throw MishapOthersName(wisp.caster ?: ctx.caster) // TODO: change

		return wisp.hex.asSpellResult
	}
}