package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.casting.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.TickingWisp

object OpMoveTargetSet : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val target = args.getVec3(0, argc)

		if (env !is WispCastEnv || env.wisp !is TickingWisp)
			throw MishapNoWisp()

		env.wisp.setTargetMovePos(target)

		return listOf()
	}
}