package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.spell.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

object OpWispHex : ConstManaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val wisp = args.getEntity(0, argc)

		if (wisp !is BaseCastingWisp)
			throw MishapInvalidIota.ofType(args[0], 0, "wisp")

		if (wisp.caster != ctx.caster)
			throw MishapOthersWisp(wisp.caster)

		return wisp.hex.asActionResult
	}
}