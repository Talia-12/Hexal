package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.entity.item.ItemEntity

object OpCompareEntities : ConstManaAction {
	override val argc = 2

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val arg0 = args.getEntity(0, argc)
		val arg1 = args.getEntity(1, argc)

		return when (arg0) {
			is ItemEntity -> {
				// returns 1 if arg1 is also an ItemEntity and they are stacks of the same kind of item
				((arg1 is ItemEntity) && (arg0.item.item.equals(arg1.item.item))).asActionResult
			}
			else -> {
				// otherwise returns if they're the same class (which should be a good way of getting the desired behaviour).
				(arg0::class == arg1::class).asActionResult
			}
		}
	}
}