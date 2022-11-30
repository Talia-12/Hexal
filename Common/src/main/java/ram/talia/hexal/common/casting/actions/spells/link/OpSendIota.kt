package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.xplat.IXplatAbstractions

object OpSendIota : SpellAction {
	private const val COST_SEND_IOTA = MediaConstants.DUST_UNIT / 100
	override val argc = 2

	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val mCast = ctx as? IMixinCastingContext

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		val linkedIndex = args.getPositiveIntUnder(0, linkThis.numLinked(), argc)
		val iota = args[1]

		val other = linkThis.getLinked(linkedIndex)

		return Triple(
			Spell(other, iota),
			COST_SEND_IOTA,
			listOf(ParticleSpray.burst(other.getPos(), 1.5))
		)
	}

	private data class Spell(val other: ILinkable<*>, val iota: Iota) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			HexalAPI.LOGGER.debug("sending $iota to $other")
			other.receiveIota(iota)
		}
	}
}