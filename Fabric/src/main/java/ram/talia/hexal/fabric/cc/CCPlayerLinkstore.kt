package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.linkable.PlayerLinkstore

class CCPlayerLinkstore(player: Player) : ServerTickingComponent, ClientTickingComponent {
	val linkstore = (player as? ServerPlayer)?.let { PlayerLinkstore(it) }
	val renderCentre = if (player.level.isClientSide) PlayerLinkstore.RenderCentre(player) else null

	fun getTransmittingTo() = linkstore!!.transmittingTo

	fun setTransmittingTo(to: Int) = linkstore!!.setTransmittingTo(to)

	fun resetTransmittingTo() = linkstore!!.resetTransmittingTo()

	override fun serverTick() = linkstore!!.checkLinks()

	override fun clientTick() {
		renderCentre!!.renderLinks()
	}

	override fun readFromNbt(tag: CompoundTag) {
		linkstore?.loadAdditionalData(tag)
	}

	override fun writeToNbt(tag: CompoundTag) {
		linkstore?.saveAdditionalData(tag)
	}
}