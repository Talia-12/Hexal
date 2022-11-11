package ram.talia.hexal.common.casting.actions.everbook

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getPattern
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.xplat.IXplatAbstractions

object OpToggleMacro : ConstMediaAction {
	override val argc = 1

	override val isGreat = true
	override val alwaysProcessGreatSpell = false
	override val causesBlindDiversion = false

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val key = args.getPattern(0, argc)

		IXplatAbstractions.INSTANCE.toggleEverbookMacro(ctx.caster, key)

		return listOf()
	}
}