package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.casting.wisp.triggers.WispTriggerRegistry
import ram.talia.hexal.api.casting.mishaps.MishapNoWisp

/**
 * Accepts a [WispTriggerRegistry.WispTriggerType] for an [ram.talia.hexal.api.casting.wisp.triggers.IWispTrigger], a wisp executing the spell will create a trigger of
 * the given type (assuming the correct args for that type of trigger are passed by the player), and attach it to the casting wisp.
 */
class OpWispSetTrigger(private val triggerType: WispTriggerRegistry.WispTriggerType<*>) : ConstMediaAction {
	override val argc = triggerType.argc

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		if (env !is WispCastEnv)
			throw MishapNoWisp()

		env.wisp.setTrigger(triggerType.makeFromArgs(env.wisp, args, env))

		return listOf()
	}
}