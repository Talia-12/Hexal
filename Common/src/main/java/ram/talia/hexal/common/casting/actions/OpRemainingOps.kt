package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.spell.casting.IMixinCastingContext

object OpRemainingOps : ConstMediaAction {
	override val argc = 0

	override fun execute(args: List<Iota>, env: CastingContext): List<Iota> {
		@Suppress("KotlinConstantConditions", "CAST_NEVER_SUCCEEDS")
		return (env as? IMixinCastingContext)?.remainingDepth()?.asActionResult ?: (-1).asActionResult
	}
}