package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.BaseLemma
import kotlin.math.ln

object OpConsumeLemma : SpellOperator {
	const val COST_FOR_OWN = ManaConstants.CRYSTAL_UNIT
	const val COST_FOR_OTHERS_PER_MEDIA = 1.5

	override val argc = 1

	override val isGreat = true

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val consumed = args.getChecked<BaseLemma>(0, argc)

		ctx.assertEntityInRange(consumed)

		val cost = when (consumed.owner?.uuid) {
			ctx.caster.uuid -> COST_FOR_OWN
			else -> (COST_FOR_OTHERS_PER_MEDIA * consumed.media).toInt()
		}

		return Triple(
			Spell(consumed),
			cost,
			listOf(ParticleSpray.burst(consumed.position(), 1.0, (ln(10.0) * 140 * ln(consumed.media/10.0 + 1)).toInt()))
		)
	}

	private data class Spell(val consumed: BaseLemma) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			val mCast = ctx as? MixinCastingContextInterface

			if (mCast != null && mCast.lemma != null)
				mCast.lemma.media += 19 * consumed.media / 20

			consumed.discard()
		}
	}
}