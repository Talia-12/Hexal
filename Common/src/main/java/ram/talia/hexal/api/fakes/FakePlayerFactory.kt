package ram.talia.hexal.api.fakes

import com.google.common.collect.Maps
import com.mojang.authlib.GameProfile
import net.minecraft.server.level.ServerLevel
import java.lang.ref.WeakReference
import java.util.*

object FakePlayerFactory {
	private val MINECRAFT = GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[Minecraft]")

	// Map of all active fake player usernames to their entities
	private val fakePlayers: MutableMap<GameProfile, FakePlayer> = Maps.newHashMap()
	private var MINECRAFT_PLAYER: WeakReference<FakePlayer>? = null

	fun getMinecraft(level: ServerLevel): FakePlayer {
		var ret: FakePlayer? = if (MINECRAFT_PLAYER != null) MINECRAFT_PLAYER!!.get() else null
		if (ret == null) {
			ret = get(level, MINECRAFT)
			MINECRAFT_PLAYER = WeakReference(ret)
		}
		return ret
	}

	/**
	 * Get a fake player with a given username,
	 * Mods should either hold weak references to the return value, or listen for a
	 * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
	 */
	operator fun get(level: ServerLevel, username: GameProfile): FakePlayer {
		if (!fakePlayers.containsKey(username)) {
			val fakePlayer = FakePlayer(level, username)
			fakePlayers[username] = fakePlayer
		}
		return fakePlayers[username]!!
	}

	fun getRandom(level: ServerLevel): FakePlayer {
		val uuid = UUID.randomUUID()
		val username = GameProfile(uuid, uuid.toString())
		val fakePlayer = FakePlayer(level, username)
		fakePlayers[username] = fakePlayer
		return fakePlayer
	}

	// TODO: make sure this gets called when a dimension is unloaded
	fun unloadLevel(level: ServerLevel) {
		fakePlayers.entries.removeIf { (_, value): Map.Entry<GameProfile, FakePlayer> -> value.level() === level }
		if (MINECRAFT_PLAYER != null && MINECRAFT_PLAYER!!.get() != null && MINECRAFT_PLAYER!!.get()!!.level() === level) // This shouldn't be strictly necessary, but lets be aggressive.
		{
			val mc: FakePlayer? = MINECRAFT_PLAYER!!.get()
			if (mc != null && mc.level() === level) {
				MINECRAFT_PLAYER = null
			}
		}
	}
}