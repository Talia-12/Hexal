package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import net.minecraft.network.chat.TranslatableComponent
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.LinkableEntity
import kotlin.math.max

object OpSendIota : SpellOperator {
	private const val COST_SEND_IOTA = ManaConstants.DUST_UNIT
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		val linkedIndex = max(args.getChecked<Double>(0, argc).toInt(), 0)
		val iota = args[1]

		if (linkedIndex >= mCast.wisp.numLinked())
			throw MishapInvalidIota(
				linkedIndex.asSpellResult[0],
				0,
				TranslatableComponent("hexcasting.mishap.invalid_value.int.between", 0, mCast.wisp.numLinked())
			)

		val other = mCast.wisp.getLinked(linkedIndex)

		ctx.assertEntityInRange(other)

		return Triple(
			Spell(other, iota),
			COST_SEND_IOTA,
			listOf(ParticleSpray.burst(other.position(), 1.5))
		)
	}

	private data class Spell(val other: LinkableEntity, val iota: SpellDatum<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			other.receiveIota(iota)
		}
	}
}