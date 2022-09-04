package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import ram.talia.hexal.api.spell.toIotaList
import ram.talia.hexal.common.entities.BaseWisp

object OpWispHex : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val wisp = args.getChecked<BaseWisp>(0, argc)

		return wisp.hex.asSpellResult
	}
}