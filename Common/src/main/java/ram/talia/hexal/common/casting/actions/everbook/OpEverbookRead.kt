package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import ram.talia.hexal.xplat.IXplatAbstractions

object OpEverbookRead : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val key = args.getChecked<HexPattern>(0, OpEverbookDelete.argc)

		val iota = IXplatAbstractions.INSTANCE.getEverbookIota(ctx.caster, key)

		val trueName = MishapOthersName.getTrueNameFromDatum(iota, ctx.caster)
		if (trueName != null)
			throw MishapOthersName(trueName)

		return listOf(iota)
	}
}