package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import ram.talia.hexal.api.HexalAPI

object OpCompareEntities : ConstManaOperator {
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val arg0 = args.getChecked<Entity>(0, argc)
		val arg1 = args.getChecked<Entity>(1, argc)

		when (arg0) {
			is ItemEntity -> {
				// returns 1 if arg1 is also an ItemEntity and they are stacks of the same kind of item
				return ((arg1 is ItemEntity) && (arg0.item.item.equals(arg1.item.item))).asSpellResult
			}
			else -> {
				// otherwise returns if they're the same class (which should be a good way of getting the desired behaviour).
				return (arg0::class == arg1::class).asSpellResult
			}
		}
	}
}