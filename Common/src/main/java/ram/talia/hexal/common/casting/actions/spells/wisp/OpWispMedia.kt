package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp

object OpWispMedia : ConstManaOperator {
	override val argc = 0

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? IMixinCastingContext

		if (mCast == null || !mCast.hasWisp())
			throw MishapNoWisp()

		return (mCast.wisp.media.toFloat() / ManaConstants.DUST_UNIT).asSpellResult
	}
}