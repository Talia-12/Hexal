package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface

object OpRemainingEvals : ConstManaOperator {
	override val argc = 0

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		@Suppress("KotlinConstantConditions")
		return (ctx as? MixinCastingContextInterface)?.remainingDepth()?.asSpellResult ?: (-1).asSpellResult
	}
}