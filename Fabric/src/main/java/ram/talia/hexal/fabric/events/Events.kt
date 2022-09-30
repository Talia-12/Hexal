package ram.talia.hexal.fabric.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.entity.player.Player

object Events {
	val LOGGOUT = EventFactory.createArrayBacked(OnLogout::class.java, { _: Player -> } as OnLogout) { callbacks -> { player: Player ->
			callbacks.forEach {it.onLogout(player) }
		} as OnLogout
	}

	public interface OnLogout : Function<Unit> {
		/**
		 * Called when a player logs out.
		 */
		fun onLogout(player: Player)
	}

	class EmptyOnLogout : OnLogout {
		override fun onLogout(player: Player) { }
	}
}