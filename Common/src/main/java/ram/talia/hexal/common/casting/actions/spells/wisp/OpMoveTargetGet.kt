package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.TickingWisp

object OpMoveTargetGet : ConstMediaAction {
	override val argc = 0

	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val mCast = ctx as? IMixinCastingContext

		if (mCast == null || !mCast.hasWisp() || mCast.wisp !is TickingWisp)
			throw MishapNoWisp()

		return (mCast.wisp as TickingWisp).getTargetMovePos()?.asActionResult ?: null.asActionResult
	}
}