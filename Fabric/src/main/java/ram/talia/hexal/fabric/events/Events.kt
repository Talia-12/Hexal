package ram.talia.hexal.fabric.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.player.Player

object Events {
	@JvmField
	val CLIENT_LOGGOUT: Event<OnClientLogout> = EventFactory.createArrayBacked(OnClientLogout::class.java, { _: Player? -> } as OnClientLogout) { callbacks ->
			{ player: Player? -> callbacks.forEach {it.onClientLogout(player) }
		} as OnClientLogout
	}

	interface OnClientLogout : Function<Unit> {
		/**
		 * Called when a player logs out.
		 */
		fun onClientLogout(player: Player?)
	}
}