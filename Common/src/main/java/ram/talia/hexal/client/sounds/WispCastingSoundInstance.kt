package ram.talia.hexal.client.sounds

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.BaseWisp
import ram.talia.hexal.common.lib.HexalSounds
import kotlin.math.max
import kotlin.math.min

// https://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/content/contraptions/components/steam/whistle/WhistleSoundInstance.java
class WispCastingSoundInstance(val wisp: BaseWisp)
	: AbstractTickableSoundInstance(HexalSounds.WISP_CASTING_CONTINUE.mainEvent, HexalSounds.WISP_CASTING_CONTINUE.category, SoundInstance.createUnseededRandom()) {

	override fun getX() = wisp.x
	override fun getY() = wisp.y
	override fun getZ() = wisp.z

	private var active: Boolean
	private var keepAlive = 0

	init {
		looping = true
		active = true
		volume = 0.05f // initialises to this, increases later
		delay = 0
		keepAlive()
	}

	fun fadeOut() {
		active = false
	}

	fun keepAlive() {
		keepAlive = 3
	}

	override fun tick() {
		HexalAPI.LOGGER.debug("attenuation type: $attenuation, is relative: $relative, position: (${getX()}, ${getY()}, ${getZ()})")

		if (active) {
			volume = min(1f, volume + .25f)
			keepAlive--
			if (keepAlive == 0)
				fadeOut()
			return
		}
		volume = max(0f, volume - .25f)
		if (volume < 0.00001)
			stop()
	}
}