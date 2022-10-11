package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.xplat.IXplatAbstractions

object OpOpenTransmit : ConstManaOperator {
	override val argc = 1

	@Suppress("CAST_NEVER_SUCCEEDS", "KotlinConstantConditions")
	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val mCtx = ctx as? IMixinCastingContext

		if (ctx.spellCircle != null || mCtx?.hasWisp() == true)
			throw MishapNoSpellCircle() // TODO a proper mishap for this.

		val playerLinkable = IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster);

		val index = args.getChecked<Double>(0, argc).toInt().coerceIn(0, playerLinkable.numLinked() - 1)

		mCtx?.setForwardingTo(index)

		return listOf()
	}
}