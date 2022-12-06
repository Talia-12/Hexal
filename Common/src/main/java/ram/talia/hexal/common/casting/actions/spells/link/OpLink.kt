package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.spell.mishaps.MishapLinkToSelf

object OpLink : SpellAction {
	const val LINK_COST = MediaConstants.SHARD_UNIT

	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)

		val linkOther = LinkableRegistry.linkableFromIota(args[0])
				?: throw MishapInvalidIota.ofType(args[0], 0, "linkable")

		if (linkThis == linkOther)
			throw MishapLinkToSelf(linkThis)

		ctx.assertVecInRange(linkOther.getPosition())

		if (!linkThis.isInRange(linkOther))
			throw MishapLocationTooFarAway(linkOther.getPosition())

		return Triple(
			Spell(linkThis, linkOther),
			LINK_COST,
			listOf(ParticleSpray.burst(linkOther.getPosition(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable, val linkOther: ILinkable) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.link(linkOther)
	}
}