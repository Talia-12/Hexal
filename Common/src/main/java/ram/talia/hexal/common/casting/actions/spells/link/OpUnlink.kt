package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpUnlink : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val linkThis = LinkableRegistry.linkableFromCastingEnvironment(env)

		val otherIndex = args.getPositiveIntUnder(0, OpSendIota.argc, linkThis.numLinked())
		val other = linkThis.getLinked(otherIndex)
			?: throw MishapInvalidIota.of(args[0], 1, "linkable.index")

		return SpellAction.Result(
			Spell(linkThis, other),
			HexalConfig.server.unlinkCost,
			listOf(ParticleSpray.burst(other.getPosition(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable, val other: ILinkable) : RenderedSpell {
		override fun cast(env: CastingEnvironment) = linkThis.unlink(other)
	}
}