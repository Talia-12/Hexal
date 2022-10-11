package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.xplat.IXplatAbstractions

object OpReadReceivedIota : ConstManaOperator {
	override val argc = 0

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		HexalAPI.LOGGER.info("pattern OpReadReceivedIota executed")
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? IMixinCastingContext

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		return listOf(linkThis.nextReceivedIota())
	}
}