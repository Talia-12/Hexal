package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerRegistry
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp

/**
 * Accepts a [WispTriggerRegistry.WispTriggerType] for an [ram.talia.hexal.api.spell.casting.triggers.IWispTrigger], a wisp executing the spell will create a trigger of
 * the given type (assuming the correct args for that type of trigger are passed by the player), and attach it to the casting wisp.
 */
class OpWispSetTrigger(private val triggerType: WispTriggerRegistry.WispTriggerType<*>) : ConstMediaAction {
	override val argc = triggerType.argc

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val mCast = env as? IMixinCastingContext

		if (mCast == null || mCast.wisp == null)
			throw MishapNoWisp()

		mCast.wisp!!.setTrigger(triggerType.makeFromArgs(mCast.wisp!!, args, env))

		return listOf()
	}
}