package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.xplat.IXplatAbstractions
import kotlin.math.max

object OpGetLinked : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? IMixinCastingContext

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		val linkedIndex = max(args.getChecked<Double>(0, OpSendIota.argc).toInt(), 0)

		if (linkedIndex >= linkThis.numLinked())
			return Widget.NULL.asSpellResult

		val other = linkThis.getLinked(linkedIndex)

		return if (ctx.isVecInRange(other.getPos())) other.asSpellResult else Widget.NULL.asSpellResult
	}
}