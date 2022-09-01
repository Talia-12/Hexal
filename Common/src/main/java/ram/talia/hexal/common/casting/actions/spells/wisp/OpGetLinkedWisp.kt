package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import kotlin.math.max

object OpGetLinkedWisp : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		val linkedIndex = max(args.getChecked<Double>(0, OpSendIota.argc).toInt(), 0)

		if (linkedIndex >= mCast.wisp.numLinked())
			return Widget.NULL.asSpellResult

		val other = mCast.wisp.getLinked(linkedIndex)

		return if (ctx.isEntityInRange(other)) other.asSpellResult else Widget.NULL.asSpellResult
	}
}