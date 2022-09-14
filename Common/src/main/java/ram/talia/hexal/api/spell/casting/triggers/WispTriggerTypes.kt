package ram.talia.hexal.api.spell.casting.triggers

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.asLong
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.casting.operators.stack.OpDuplicateN
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.BaseCastingWisp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

object WispTriggerTypes {
	@JvmField
	val TickTiggerType = object : WispTriggerRegistry.WispTriggerType<TickTrigger>(HexalAPI.modLoc("trigger/tick")) {
		override val argc = 1

		override fun makeFromArgs(wisp: BaseCastingWisp, args: List<SpellDatum<*>>, ctx: CastingContext): TickTrigger {
			val countDouble = args.getChecked<Double>(0, argc)

			if (abs(countDouble.roundToInt() - countDouble) >= 0.05f)
				throw MishapInvalidIota(
					args[0],
					0,
					"hexcasting.mishap.invalid_value.int".asTranslatedComponent()
				)

			return TickTrigger(countDouble.roundToLong() + ctx.world.gameTime)
		}

		override fun fromNbt(tag: Tag, level: ServerLevel) = TickTrigger.readFromNbt(tag, level)
	}

	@JvmField
	val CommTiggerType = object : WispTriggerRegistry.WispTriggerType<CommTrigger>(HexalAPI.modLoc("trigger/comm")) {
		override val argc = 0

		override fun makeFromArgs(wisp: BaseCastingWisp, args: List<SpellDatum<*>>, ctx: CastingContext) = CommTrigger()

		override fun fromNbt(tag: Tag, level: ServerLevel): CommTrigger? {
			return CommTrigger.readFromNbt(tag, level)
		}
	}
}

data class TickTrigger(val tick: Long) : IWispTrigger {
	var hasTriggered = false

	override fun shouldTrigger(wisp: BaseCastingWisp): Boolean {
//		HexalAPI.LOGGER.info("checking should trigger $wisp at tick ${wisp.level.gameTime}, only if >= $tick")

		if (wisp.level.gameTime < tick)
			return false

		hasTriggered = true
		return true
	}

	override fun shouldRemoveTrigger(wisp: BaseCastingWisp) = hasTriggered

	override fun getTriggerType() = WispTriggerTypes.TickTiggerType

	override fun writeToNbt(): Tag {
		return LongTag.valueOf(tick)
	}

	companion object {
		fun readFromNbt(tag: Tag, level: ServerLevel): TickTrigger? {
			return TickTrigger(tag.asLong)
		}
	}
}

class CommTrigger : IWispTrigger {
	var hasTriggered = false

	override fun shouldTrigger(wisp: BaseCastingWisp): Boolean {
		if (wisp.numRemainingIota() == 0)
			return false

		hasTriggered = true
		return true
	}

	override fun shouldRemoveTrigger(wisp: BaseCastingWisp) = hasTriggered

	override fun getTriggerType() = WispTriggerTypes.CommTiggerType

	override fun writeToNbt(): Tag {
		return ByteTag.ZERO
	}

	companion object {
		fun readFromNbt(tag: Tag, level: ServerLevel): CommTrigger {
			return CommTrigger()
		}
	}
}