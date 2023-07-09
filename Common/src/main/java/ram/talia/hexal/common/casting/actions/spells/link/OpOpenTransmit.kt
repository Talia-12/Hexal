package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoLinked
import ram.talia.hexal.api.spell.mishaps.MishapNonPlayer
import ram.talia.hexal.xplat.IXplatAbstractions

object OpOpenTransmit : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		if (env !is PlayerBasedCastEnv)
			throw MishapNonPlayer()

		val playerLinkable = IXplatAbstractions.INSTANCE.getLinkstore(env.caster)

		if (playerLinkable.numLinked() == 0)
			throw MishapNoLinked(playerLinkable)

		val index = args.getPositiveIntUnder(0, argc, playerLinkable.numLinked())

		IXplatAbstractions.INSTANCE.setPlayerTransmittingTo(env.caster, index)

		return listOf()
	}
}