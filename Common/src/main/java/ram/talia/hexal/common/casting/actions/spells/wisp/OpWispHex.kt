package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.spell.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.xplat.IXplatAbstractions

object OpWispHex : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val wisp = args.getEntity(0, argc)

		if (wisp !is BaseCastingWisp)
			throw MishapInvalidIota.ofType(args[0], 0, "wisp")

		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)
		if (!(wisp.caster == ctx.caster ||
				wisp.whiteListContains(linkThis) ||
				(linkThis as? BaseCastingWisp)?.caster?.let { wisp.whiteListContains(IXplatAbstractions.INSTANCE.getLinkstore(it as ServerPlayer)) } == true))
			throw MishapOthersWisp(wisp.caster)

		return wisp.serHex.getIotas(ctx.world).asActionResult
	}
}