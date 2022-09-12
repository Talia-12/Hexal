package ram.talia.hexal.api.spell.casting.triggers

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.common.entities.BaseCastingWisp

interface IWispTrigger {
	fun shouldTrigger(wisp: BaseCastingWisp): Boolean

	fun shouldRemoveTrigger(wisp: BaseCastingWisp): Boolean

	fun getTriggerType(): WispTriggerRegistry.WispTriggerType<*>

	fun writeToNbt(): Tag
}