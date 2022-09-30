package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.everbook.Everbook
import ram.talia.hexal.common.network.MsgSendEverbookAck
import ram.talia.hexal.xplat.IClientXplatAbstractions

class CCEverbook(private val player: Player) : AutoSyncedComponent {
	private var everbook: Everbook? = null
		set(value) {
			if (field == null)
				field = value
		}

	init {
		if (player.level.isClientSide) {
			everbook = Everbook.fromDisk(player.uuid)

			IClientXplatAbstractions.INSTANCE.sendPacketToServer(MsgSendEverbookAck())
		}
	}

	/**
	 * Data stored on the client rather than the server
	 */
	override fun readFromNbt(tag: CompoundTag) { }

	/**
	 * Data stored on the client rather than the server
	 */
	override fun writeToNbt(tag: CompoundTag) { }
}