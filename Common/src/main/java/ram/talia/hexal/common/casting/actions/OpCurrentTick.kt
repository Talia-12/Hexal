package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota

object OpCurrentTick : ConstMediaAction {
	override val argc = 0

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		return (env.world.gameTime).asActionResult
	}
}