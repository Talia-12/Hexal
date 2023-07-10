package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.IMediaEntity
import kotlin.math.ln

object OpConsumeWisp : SpellAction {
	const val TAG_CONSUMED_MEDIA = "hexal:consumed_media"

	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val consumed = args.getEntity(0, argc) as? IMediaEntity<*> ?: throw MishapInvalidIota.ofType(args[0], 0, "consumable_entity")

		env.assertEntityInRange(consumed.get())

		val consumer: Either<BaseCastingWisp, ServerPlayer?> = if (env is WispCastEnv) Either.left(env.wisp) else Either.right(env.caster)

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
			throw IllegalStateException("call cast(env, image) instead.")
		}

		override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
			HexalAPI.LOGGER.debug("cast method of Spell of OpConsumeWisp triggered targeting {}", consumed)

			return if (env is WispCastEnv) {
				env.wisp.addMedia(19 * consumed.media / 20)
				consumed.get().discard()
				null
			} else {
				val userData = image.userData.copy()
				val consumedMedia = userData.getLong(TAG_CONSUMED_MEDIA)
				userData.putLong(TAG_CONSUMED_MEDIA, consumedMedia + 19 * consumed.media / 20)
				consumed.get().discard()
				image.copy(userData = userData)
			}
		}
	}
}