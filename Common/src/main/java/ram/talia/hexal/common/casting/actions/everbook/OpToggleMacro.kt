package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.math.HexPattern
import ram.talia.hexal.xplat.IXplatAbstractions

object OpToggleMacro : ConstManaOperator {
	override val argc = 1

	override val isGreat = true
	override val alwaysProcessGreatSpell = false
	override val causesBlindDiversion = false

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val key = args.getChecked<HexPattern>(0, argc)

		IXplatAbstractions.INSTANCE.toggleEverbookMacro(ctx.caster, key)

		return listOf()
	}
}