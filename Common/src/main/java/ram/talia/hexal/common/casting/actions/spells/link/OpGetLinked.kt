package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.linkable.LinkableRegistry

object OpGetLinked : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val linkThis = LinkableRegistry.linkableFromCastingContext(ctx)

		val linkedIndex = args.getPositiveInt(0, OpSendIota.argc)

		if (linkedIndex >= linkThis.numLinked())
			return null.asActionResult

		val other = linkThis.getLinked(linkedIndex) ?: return null.asActionResult

		return if (ctx.isVecInRange(other.getPosition())) other.asActionResult else null.asActionResult
	}
}