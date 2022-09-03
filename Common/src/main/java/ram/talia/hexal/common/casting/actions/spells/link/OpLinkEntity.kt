package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.BaseWisp
import ram.talia.hexal.common.entities.LinkableEntity

object OpLinkEntity : SpellOperator {
	const val LINK_COST = ManaConstants.SHARD_UNIT

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		HexalAPI.LOGGER.info("attempting to link")

		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		val thisWisp = mCast.wisp
		val other = args.getChecked<LinkableEntity>(0, argc)

		HexalAPI.LOGGER.info("attempting to link $thisWisp to $other")

		if ((thisWisp.position() - other.position()).lengthSqr() > thisWisp.maxSqrCastingDistance())
			throw MishapEntityTooFarAway(other)

		return Triple(
			Spell(thisWisp, other),
			LINK_COST,
			listOf(ParticleSpray.burst(other.position(), 1.5))
		)
	}

	private data class Spell(val thisWisp: BaseWisp, val other: LinkableEntity) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			HexalAPI.LOGGER.info("calling link on $thisWisp to $other")
			thisWisp.link(other)
		}
	}
}