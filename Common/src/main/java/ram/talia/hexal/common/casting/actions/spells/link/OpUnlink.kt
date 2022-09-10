package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import net.minecraft.network.chat.TranslatableComponent
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.BaseWisp
import ram.talia.hexal.common.entities.LinkableEntity
import kotlin.math.max

object OpUnlink : SpellOperator {
	const val UNLINK_COST = 2 * ManaConstants.DUST_UNIT

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		val thisWisp = mCast.wisp
		val otherIndex = max(args.getChecked<Double>(0, OpSendIota.argc).toInt(), 0)

		if (otherIndex >= mCast.wisp.numLinked())
			throw MishapInvalidIota(
				otherIndex.asSpellResult[0],
				0,
				TranslatableComponent("hexcasting.mishap.invalid_value.int.between", 0, mCast.wisp.numLinked())
			)

		val other = thisWisp.getLinked(otherIndex)

		return Triple(
			Spell(thisWisp, other),
			UNLINK_COST,
			listOf(ParticleSpray.burst(other.getPos(), 1.5))
		)
	}

	private data class Spell(val thisWisp: BaseWisp, val other: ILinkable<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			thisWisp.unlink(other)
		}

	}
}