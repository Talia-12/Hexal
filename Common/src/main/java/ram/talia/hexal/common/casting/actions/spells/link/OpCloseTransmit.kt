package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.xplat.IXplatAbstractions

object OpCloseTransmit : ConstMediaAction {
	override val argc = 0

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		IXplatAbstractions.INSTANCE.resetPlayerTransmittingTo(env.caster)
		return listOf()
	}
}