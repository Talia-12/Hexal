package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpUnlink : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)

		val otherIndex = args.getPositiveIntUnder(0, OpSendIota.argc, linkThis.numLinked())
		val other = linkThis.getLinked(otherIndex)

		return Triple(
			Spell(linkThis, other),
			HexalConfig.server.unlinkCost,
			listOf(ParticleSpray.burst(other.getPosition(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable, val other: ILinkable) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.unlink(other)
	}
}