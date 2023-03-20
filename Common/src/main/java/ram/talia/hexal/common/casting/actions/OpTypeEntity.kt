package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.asActionResult

object OpTypeEntity : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		return args.getEntity(0, argc).type.asActionResult
	}
}