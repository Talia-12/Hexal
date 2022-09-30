package ram.talia.hexal.fabric.cc

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.math.HexPattern
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.everbook.Everbook
import ram.talia.hexal.common.network.MsgRemoveEverbookAck
import ram.talia.hexal.common.network.MsgSendEverbookSyn
import ram.talia.hexal.common.network.MsgSetEverbookAck
import ram.talia.hexal.fabric.events.Events
import ram.talia.hexal.xplat.IClientXplatAbstractions
import ram.talia.hexal.xplat.IXplatAbstractions

class CCEverbook(private val player: Player) : AutoSyncedComponent {
	private var everbook: Everbook? = null
		set(value) {
			if (field == null)
				field = value
		}

	init {
		if (player.level.isClientSide) {
			everbook = Everbook.fromDisk(player.uuid)

			IClientXplatAbstractions.INSTANCE.sendPacketToServer(MsgSendEverbookSyn(everbook!!))
		}
	}

	fun getIota(key: HexPattern, level: ServerLevel) = everbook?.getIota(key, level) ?: SpellDatum.make(Widget.NULL)
	fun setIota(key: HexPattern, iota: SpellDatum<*>) {
		if (player.level.isClientSide) throw Exception("CCEverbook.setIota can only be called on the server") // TODO

		if (everbook != null) {
			everbook!!.setIota(key, iota)
			IXplatAbstractions.INSTANCE.sendPacketToPlayer(player as ServerPlayer, MsgSetEverbookAck(key, iota.serializeToNBT()))
		}
	}

	fun setFullEverbook(everbook: Everbook) {
		if (player.level.isClientSide) throw Exception("CCEverbook.setFullEverbook can only be called on the server") // TODO
		this.everbook = everbook
	}

	fun removeIota(key: HexPattern) {
		if (player.level.isClientSide) throw Exception("CCEverbook.removeIota can only be called on the server") // TODO
		everbook?.removeIota(key)

		IXplatAbstractions.INSTANCE.sendPacketToPlayer(player as ServerPlayer, MsgRemoveEverbookAck(key))
	}

	fun setClientIota(key: HexPattern, iota: CompoundTag) = everbook!!.setIota(key, iota)
	fun removeClientIota(key: HexPattern) = everbook!!.removeIota(key)

	//region read/write Nbt (unused)
	/**
	 * Data stored on the client rather than the server
	 */
	override fun readFromNbt(tag: CompoundTag) { }

	/**
	 * Data stored on the client rather than the server
	 */
	override fun writeToNbt(tag: CompoundTag) { }
	//endregion
	companion object {
		init {
			Events.CLIENT_LOGGOUT.register(Companion::saveToDisk as Events.OnClientLogout)
		}

		private fun saveToDisk(player: Player?) {
			if (player != null)
				HexalCardinalComponents.EVERBOOK.get(player).everbook?.saveToDisk()
		}
	}
}