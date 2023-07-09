package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.IMediaEntity
import kotlin.math.ln

object OpConsumeWisp : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val consumed = args.getEntity(0, argc) as? IMediaEntity<*> ?: throw MishapInvalidIota.ofType(args[0], 0, "consumable_entity")

		env.assertEntityInRange(consumed.get())

		val mCast = env as? IMixinCastingContext

		val consumer: Either<BaseCastingWisp, ServerPlayer> = if (mCast != null && mCast.wisp != null) Either.left(mCast.wisp) else Either.right(env.caster)

		HexalAPI.LOGGER.debug("consumer: {}, {}", consumer, consumed.fightConsume(consumer))

		val cost = when (consumed.fightConsume(consumer)) {
			false  -> HexalConfig.server.consumeWispOwnCost
			true   -> (HexalConfig.server.consumeWispOthersCostPerMedia * consumed.media).toInt()
		}

		HexalAPI.LOGGER.debug("cost to consume {} is {}", consumed, cost)

		return SpellAction.Result(
			Spell(consumed),
			cost,
			listOf(ParticleSpray.burst(consumed.get().position(), 1.0, (ln(10.0) * 14 * ln(consumed.media/10.0 + 1)).toInt()))
		)
	}

	private data class Spell(val consumed: IMediaEntity<*>) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {
			HexalAPI.LOGGER.debug("cast method of Spell of OpConsumeWisp triggered targeting {}", consumed)

			val mCast = env as? IMixinCastingContext

			if (mCast != null && mCast.wisp != null)
				mCast.wisp!!.addMedia(19 * consumed.media / 20) // using addMedia to prevent overflow.
			else if (mCast != null)
				mCast.consumedMedia += 19 * consumed.media / 20

			HexalAPI.LOGGER.debug("discarding {}", consumed)
			consumed.get().discard()
		}
	}
}