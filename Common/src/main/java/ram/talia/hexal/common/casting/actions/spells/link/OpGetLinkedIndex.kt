package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpGetLinkedIndex : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)
		val linkOther = LinkableRegistry.linkableFromIota(args[0], ctx.world) ?: return (-1).asActionResult

		return linkThis.getLinkedIndex(linkOther).asActionResult
	}
}