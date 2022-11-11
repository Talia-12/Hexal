package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.entity.item.ItemEntity
import ram.talia.hexal.api.asActionResult

object OpTypeEntity : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		// returns an item type if the entity is an item entity, or an entity type otherwise.
		return when (val arg = args.getEntity(0, argc)) {
			is ItemEntity -> arg.item.item.asActionResult
			else -> arg.type.asActionResult
		}
	}
}