package ram.talia.hexal.api.spell.casting.triggers

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getLong
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.asLong
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.TickingWisp

object WispTriggerTypes {
	@JvmField
	val TICK_TRIGGER_TYPE = object : WispTriggerRegistry.WispTriggerType<TickTrigger>(HexalAPI.modLoc("trigger/tick")) {
		override val argc = 1

		override fun makeFromArgs(wisp: BaseCastingWisp, args: List<Iota>, ctx: CastingContext): TickTrigger {
			val count = args.getLong(0, argc)

			return TickTrigger(count + ctx.world.gameTime)
		}

		override fun fromNbt(tag: Tag, level: ServerLevel) = TickTrigger.readFromNbt(tag, level)
	}

	@JvmField
	val COMM_TRIGGER_TYPE = object : WispTriggerRegistry.WispTriggerType<CommTrigger>(HexalAPI.modLoc("trigger/comm")) {
		override val argc = 0

		override fun makeFromArgs(wisp: BaseCastingWisp, args: List<Iota>, ctx: CastingContext) = CommTrigger()

		override fun fromNbt(tag: Tag, level: ServerLevel) = CommTrigger.readFromNbt(tag, level)
	}

	@JvmField
	val MOVE_TRIGGER_TYPE = object : WispTriggerRegistry.WispTriggerType<MoveTrigger>(HexalAPI.modLoc("trigger/move")) {
		override val argc = 0

		override fun makeFromArgs(wisp: BaseCastingWisp, args: List<Iota>, ctx: CastingContext) = MoveTrigger()

		override fun fromNbt(tag: Tag, level: ServerLevel) = MoveTrigger.readFromNbt(tag, level)
	}
}

data class TickTrigger(val tick: Long) : IWispTrigger {
	override var hasTriggered = false

	override fun shouldTrigger(wisp: BaseCastingWisp): Boolean {

		if (wisp.level.gameTime < tick)
			return false

		hasTriggered = true
		return true
	}

	override fun getTriggerType() = WispTriggerTypes.TICK_TRIGGER_TYPE

	override fun writeToNbt() = LongTag.valueOf(tick)

	companion object {
		fun readFromNbt(tag: Tag, level: ServerLevel) = TickTrigger(tag.asLong)
	}
}

class CommTrigger : IWispTrigger {
	override var hasTriggered = false

	override fun shouldTrigger(wisp: BaseCastingWisp): Boolean {
		if (wisp.numRemainingIota() == 0)
			return false

		hasTriggered = true
		return true
	}

	override fun getTriggerType() = WispTriggerTypes.COMM_TRIGGER_TYPE

	override fun writeToNbt() = ByteTag.ZERO

	companion object {
		fun readFromNbt(tag: Tag, level: ServerLevel) = CommTrigger()
	}
}

class MoveTrigger : IWispTrigger {
	override var hasTriggered = false

	override fun shouldTrigger(wisp: BaseCastingWisp): Boolean {
		if (wisp !is TickingWisp || !wisp.reachedTargetPos())
			return false

		hasTriggered = true
		return true
	}

	override fun getTriggerType() = WispTriggerTypes.MOVE_TRIGGER_TYPE

	override fun writeToNbt() = ByteTag.ZERO

	companion object {
		fun readFromNbt(tag: Tag, level: ServerLevel) = MoveTrigger()
	}
}