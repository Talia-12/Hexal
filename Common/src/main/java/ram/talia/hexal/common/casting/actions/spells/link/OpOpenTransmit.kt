package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoLinked
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp
import ram.talia.hexal.xplat.IXplatAbstractions

object OpOpenTransmit : ConstManaOperator {
	override val argc = 1

	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val mCtx = ctx as? IMixinCastingContext

		if (ctx.spellCircle != null || mCtx?.hasWisp() == true)
			throw MishapNoWisp()

		val playerLinkable = IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)

		if (playerLinkable.numLinked() == 0)
			throw MishapNoLinked(playerLinkable)

		val index = args.getChecked<Double>(0, argc).toInt().coerceIn(0, playerLinkable.numLinked() - 1)

		IXplatAbstractions.INSTANCE.setPlayerTransmittingTo(ctx.caster, index)

		return listOf()
	}
}