package ram.talia.hexal.common.entities

import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import kotlin.math.max

interface IMediaEntity<T : Entity> {
	/**
	 * Should always be >= 0.
	 */
	var media: Long

	val isConsumable: Boolean

	/**
	 * Returns whether the IMediaEntity should struggle against being consumed
	 * (i.e. cost proportional to stored media if it struggles, or fixed cost otherwise.)
	 */
	fun fightConsume(consumer: Either<BaseCastingWisp, ServerPlayer?>): Boolean

	fun get(): T

	fun addMedia(dMedia: Long) {
		val new = media + dMedia
		// preventing overflow above Int.MAX_VALUE and disallowing values below 0.
		media = if (dMedia > 0 && media > new) Long.MAX_VALUE - 1 else max(new, 0)
	}
}