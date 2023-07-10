package ram.talia.hexal.api.casting.wisp.triggers

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.common.entities.BaseCastingWisp

object WispTriggerRegistry {
	const val TAG_TYPE = "type"
	const val TAG_WISP_TRIGGER = "trigger"

	private val triggerTypes: MutableMap<ResourceLocation, WispTriggerType<*>> = mutableMapOf()

	class RegisterWispTriggerTypeException(msg: String) : Exception(msg)
	class InvalidWispTriggerTypeException(msg: String) : Exception(msg)

	init {
		registerWispTriggerType(WispTriggerTypes.TICK_TRIGGER_TYPE)
		registerWispTriggerType(WispTriggerTypes.COMM_TRIGGER_TYPE)
		registerWispTriggerType(WispTriggerTypes.MOVE_TRIGGER_TYPE)
	}

	fun registerWispTriggerType(type: WispTriggerType<*>) {
		if (triggerTypes.containsKey(type.id))
			throw RegisterWispTriggerTypeException("LinkableRegistry already contains resource id ${type.id}")

		triggerTypes[type.id] = type
	}

	abstract class WispTriggerType<T : IWispTrigger> (val id: ResourceLocation) {
		abstract val argc: Int

		/**
		 * Build an [IWispTrigger] of this type from spell arguments, to be used in OpWispSetTrigger
		 */
		abstract fun makeFromArgs(wisp: BaseCastingWisp, args: List<Iota>, env: CastingEnvironment): T

		/**
		 * Takes a tag from [IWispTrigger.writeToNbt] and wraps it inside a [CompoundTag] that also stores a reference to the [WispTriggerType] of the
		 * [IWispTrigger], meaning that the loader will know which [WispTriggerType] to use to restore the [IWispTrigger].
		 */
		fun wrapNbt(itTag: Tag): CompoundTag {
			val tag = CompoundTag()
			tag.putString(TAG_TYPE, "$id")
			tag.put(TAG_WISP_TRIGGER, itTag)
			return tag
		}

		/**
		 * Takes a tag containing a saved [IWispTrigger] of the type specified by this [WispTriggerType] and restores the [IWispTrigger].
		 */
		abstract fun fromNbt(tag: Tag, level: ServerLevel): T?
	}

	/**
	 * Pass an [IWispTrigger] and get back a [CompoundTag] that can be used by [fromNbt] to restore the [IWispTrigger]
	 */
	fun wrapNbt(trigger: IWispTrigger) = trigger.getTriggerType().wrapNbt(trigger.writeToNbt())

	/**
	 * Restore an [IWispTrigger] saved by [wrapNbt].
	 */
	fun fromNbt(tag: CompoundTag, level: ServerLevel): IWispTrigger? {
		val typeId = tag.getString(TAG_TYPE)
		if (!ResourceLocation.isValidResourceLocation(typeId))
			throw InvalidWispTriggerTypeException("$typeId is not a valid resource location")

		val type = triggerTypes[ResourceLocation(typeId)] ?: throw InvalidWispTriggerTypeException("no WispTriggerType registered for $typeId")

		return type.fromNbt(tag.get(TAG_WISP_TRIGGER)!!, level)
	}
}