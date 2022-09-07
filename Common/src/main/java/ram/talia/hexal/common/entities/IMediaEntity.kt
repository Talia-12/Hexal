package ram.talia.hexal.common.entities

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

interface IMediaEntity<T : Entity> {
	var media: Int
	val isConsumable: Boolean

	/**
	 * Returns whether the IMediaEntity should struggle against being consumed
	 * (i.e. cost proportional to stored media if it struggles, or fixed cost otherwise.)
	 */
	fun fightConsume(caster: ServerPlayer): Boolean

	fun get(): T

	fun addMedia(dMedia: Int) {
		media += dMedia
	}
}