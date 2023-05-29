package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpSendIota : SpellAction {
	override val argc = 2

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)

		val linkedIndex = args.getPositiveIntUnder(0, linkThis.numLinked(), argc)
		val iota = args[1]

		val other = linkThis.getLinked(linkedIndex) ?: return null

		return Triple(
			Spell(other, iota),
			HexalConfig.server.sendIotaCost,
			listOf()
		)
	}

	private data class Spell(val other: ILinkable, val iota: Iota) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			HexalAPI.LOGGER.debug("sending {} to {}", iota, other)
			other.receiveIota(LinkableRegistry.linkableFromCastingContext(ctx), iota)
		}
	}
}