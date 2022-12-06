package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpUnlink : SpellAction {
	const val UNLINK_COST = 2 * MediaConstants.DUST_UNIT

	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)

		val otherIndex = args.getPositiveIntUnder(0, OpSendIota.argc, linkThis.numLinked())
		val other = linkThis.getLinked(otherIndex)

		return Triple(
			Spell(linkThis, other),
			UNLINK_COST,
			listOf(ParticleSpray.burst(other.getPosition(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable, val other: ILinkable) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.unlink(other)
	}
}