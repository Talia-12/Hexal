package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.IMediaEntity
import kotlin.math.ln

object OpConsumeWisp : SpellOperator {
	const val COST_FOR_OWN = ManaConstants.SHARD_UNIT
	const val COST_FOR_OTHERS_PER_MEDIA = 1.5

	override val argc = 1

	override val isGreat = true

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val consumed = args.getChecked<IMediaEntity<*>>(0, argc)

		ctx.assertEntityInRange(consumed.get())

		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		val consumer: Either<BaseCastingWisp, ServerPlayer> = if (mCast != null && mCast.wisp != null) Either.left(mCast.wisp) else Either.right(ctx.caster)

		HexalAPI.LOGGER.info("consumer: $consumer, ${consumed.fightConsume(consumer)}")

		val cost = when (consumed.fightConsume(consumer)) {
			false  -> COST_FOR_OWN
			true   -> (COST_FOR_OTHERS_PER_MEDIA * consumed.media).toInt()
		}

		HexalAPI.LOGGER.info("cost to consume $consumed is $cost")

		return Triple(
			Spell(consumed),
			cost,
			listOf(ParticleSpray.burst(consumed.get().position(), 1.0, (ln(10.0) * 14 * ln(consumed.media/10.0 + 1)).toInt()))
		)
	}

	private data class Spell(val consumed: IMediaEntity<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			HexalAPI.LOGGER.info("cast method of Spell of OpConsumeWisp triggered targeting $consumed")

			@Suppress("CAST_NEVER_SUCCEEDS")
			val mCast = ctx as? MixinCastingContextInterface

			if (mCast != null && mCast.wisp != null)
				mCast.wisp.media += 19 * consumed.media / 20

			HexalAPI.LOGGER.info("discarding $consumed")
			consumed.get().discard()
		}
	}
}