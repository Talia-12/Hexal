package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.casting.wisp.IMixinCastingContext
import ram.talia.hexal.api.casting.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.TickingWisp

object OpMoveSpeedGet : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val mCast = env as? IMixinCastingContext

        if (mCast == null || !mCast.hasWisp() || mCast.wisp !is TickingWisp)
            throw MishapNoWisp()

        return ((mCast.wisp as TickingWisp).currentMoveMultiplier * TickingWisp.BASE_MAX_SPEED_PER_TICK).asActionResult
    }
}