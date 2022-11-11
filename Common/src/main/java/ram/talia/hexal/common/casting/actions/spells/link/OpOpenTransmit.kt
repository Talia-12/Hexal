package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoLinked
import ram.talia.hexal.api.spell.mishaps.MishapNonPlayer
import ram.talia.hexal.xplat.IXplatAbstractions

object OpOpenTransmit : ConstMediaAction {
	override val argc = 1

	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val mCtx = ctx as? IMixinCastingContext

		if (ctx.spellCircle != null || mCtx?.hasWisp() == true)
			throw MishapNonPlayer()

		val playerLinkable = IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)

		if (playerLinkable.numLinked() == 0)
			throw MishapNoLinked(playerLinkable)

		val index = args.getPositiveIntUnder(0, argc, playerLinkable.numLinked())

		IXplatAbstractions.INSTANCE.setPlayerTransmittingTo(ctx.caster, index)

		return listOf()
	}
}