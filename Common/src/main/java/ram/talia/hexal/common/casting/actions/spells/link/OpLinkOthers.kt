package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.spell.mishaps.MishapLinkToSelf

object OpLinkOthers : SpellAction {
	override val argc = 2

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val linkThis = LinkableRegistry.linkableFromIota(args[0])
				?: throw MishapInvalidIota.ofType(args[0], 1, "linkable")
		val linkOther = LinkableRegistry.linkableFromIota(args[1])
				?: throw MishapInvalidIota.ofType(args[1], 0, "linkable")

		if (linkThis == linkOther)
			throw MishapLinkToSelf(linkThis)

		ctx.assertVecInRange(linkThis.getPos())
		ctx.assertVecInRange(linkOther.getPos())

		if (!linkThis.isInRange(linkOther))
			throw MishapLocationTooFarAway(linkOther.getPos())

		return Triple(
			Spell(linkThis, linkOther),
			OpLink.LINK_COST,
			listOf(ParticleSpray.burst(linkThis.getPos(), 1.5), ParticleSpray.burst(linkOther.getPos(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable<*>, val linkOther: ILinkable<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.link(linkOther)
	}
}