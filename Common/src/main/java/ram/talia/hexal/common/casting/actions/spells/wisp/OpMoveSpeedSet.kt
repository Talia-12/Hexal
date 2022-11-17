package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.TickingWisp
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min

object OpMoveSpeedSet : SpellOperator {
    const val BASE_COST = ManaConstants.DUST_UNIT

    override val argc = 1

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val newMax = abs(args.getChecked<Double>(0, OpMoveTargetSet.argc))
        val newMult = newMax / TickingWisp.BASE_MAX_SPEED_PER_TICK

        val mCast = ctx as? IMixinCastingContext

        if (mCast == null || !mCast.hasWisp() || mCast.wisp !is TickingWisp)
            throw MishapNoWisp()

        return Triple(
                Spell(mCast.wisp as TickingWisp, newMult),
                (BASE_COST * newMult * newMult).toInt(),
                listOf(ParticleSpray.burst(mCast.wisp.position(), min(1.0, ln(newMult))))
        )
    }

    private data class Spell(val wisp: TickingWisp, val newMult: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            wisp.currentMoveMultiplier = newMult.toFloat()
        }
    }
}