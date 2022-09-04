package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface

object OpReadReceivedIota : ConstManaOperator {
	override val argc = 0

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		HexalAPI.LOGGER.info("pattern OpReadReceivedIota executed")
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		HexalAPI.LOGGER.info("executed by wisp ${mCast.wisp.uuid}")

		return listOf(mCast.wisp.nextReceivedIota())
	}
}