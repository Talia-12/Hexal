package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerRegistry
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp

/**
 * Accepts a [WispTriggerRegistry.WispTriggerType] for an [ram.talia.hexal.api.spell.casting.triggers.IWispTrigger], a wisp executing the spell will create a trigger of
 * the given type (assuming the correct args for that type of trigger are passed by the player), and attach it to the casting wisp.
 */
class OpWispSetTrigger(private val triggerType: WispTriggerRegistry.WispTriggerType<*>) : ConstMediaAction {
	override val argc = triggerType.argc

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? IMixinCastingContext

		if (mCast == null || mCast.wisp == null)
			throw MishapNoWisp()

		HexalAPI.LOGGER.debug("Setting ${mCast.wisp} trigger to $triggerType")

		mCast.wisp!!.setTrigger(triggerType.makeFromArgs(mCast.wisp!!, args, ctx))

		return listOf()
	}
}