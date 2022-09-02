package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.BaseWisp
import ram.talia.hexal.common.entities.LinkableEntity

object OpUnlinkWisp : SpellOperator {
	const val UNLINK_COST = ManaConstants.SHARD_UNIT

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		val thisWisp = mCast.wisp
		val other = args.getChecked<LinkableEntity>(0, argc) //TODO: change to be based on index

		return Triple(
			Spell(thisWisp, other),
			UNLINK_COST,
			listOf(ParticleSpray.burst(other.position(), 1.5))
		)
	}

	private data class Spell(val thisWisp: BaseWisp, val other: LinkableEntity) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			thisWisp.unlink(other)
		}

	}
}