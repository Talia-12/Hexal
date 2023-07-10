package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.casting.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.TickingWisp

object OpMoveTargetGet : ConstMediaAction {
	override val argc = 0

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		if (env !is WispCastEnv || env.wisp !is TickingWisp)
			throw MishapNoWisp()

		return env.wisp.getTargetMovePos()?.asActionResult ?: null.asActionResult
	}
}