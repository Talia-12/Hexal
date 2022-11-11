package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.xplat.IXplatAbstractions

object OpNumReceivedIota : ConstMediaAction {
	override val argc = 0

	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		HexalAPI.LOGGER.debug("pattern OpNumReceivedIota executed")

		val mCast = ctx as? IMixinCastingContext

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		return linkThis.numRemainingIota().asActionResult
	}
}