package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.linkable.PlayerLinkstore
import ram.talia.hexal.api.nbt.toCompoundTagList
import ram.talia.hexal.api.nbt.toIRenderCentreList
import ram.talia.hexal.api.nbt.toSyncTag
import ram.talia.hexal.client.playLinkParticles

class CCPlayerLinkstore(private val player: Player) : ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {
	val linkstore = (player as? ServerPlayer)?.let { PlayerLinkstore(it) }
	private val ownerRenderCentre = if (player.level.isClientSide) PlayerLinkstore.RenderCentre(player) else null
	var renderLinks: MutableList<ILinkable.IRenderCentre> = mutableListOf()

	private val renderLinksToAdd: MutableList<ILinkable<*>> = mutableListOf()
	private val renderLinksToRemove: MutableList<ILinkable<*>> = mutableListOf()

	fun addRenderLink(link: ILinkable<*>) {
		renderLinksToAdd.add(link)
		HexalCardinalComponents.PLAYER_LINKSTORE.sync(player)
	}
	fun removeRenderLink(link: ILinkable<*>) {
		renderLinksToRemove.add(link)
		HexalCardinalComponents.PLAYER_LINKSTORE.sync(player)
	}

	fun getTransmittingTo() = linkstore!!.transmittingTo

	fun setTransmittingTo(to: Int) = linkstore!!.setTransmittingTo(to)

	fun resetTransmittingTo() = linkstore!!.resetTransmittingTo()

	override fun serverTick() = linkstore!!.pruneLinks()

	override fun clientTick() {
		val iter = renderLinks.iterator()

		while (iter.hasNext()) {
			val other = iter.next()
			if (other.shouldRemove())
				iter.remove()
			else
				playLinkParticles(ownerRenderCentre!!, other, player.random, player.level)
		}
	}

	override fun readFromNbt(tag: CompoundTag) {
		linkstore?.loadAdditionalData(tag)
	}

	override fun writeToNbt(tag: CompoundTag) {
		linkstore?.saveAdditionalData(tag)
	}

	override fun writeSyncPacket(buf: FriendlyByteBuf, recipient: ServerPlayer) {
		HexalAPI.LOGGER.info("attempting to sync CCPlayerLinkstore for $player")

		// ensures that first sync when player joins properly syncs all the links.
		val linksToAdd = when (renderLinksToAdd.size == 0 && renderLinksToRemove.size == 0) {
			false -> renderLinksToAdd
			true -> linkstore!!.renderLinks
		}

		val tag = CompoundTag()
		tag.put(TAG_RENDER_LINKS_ADD, linksToAdd.toSyncTag())
		tag.put(TAG_RENDER_LINKS_REMOVE, renderLinksToRemove.toSyncTag())
		buf.writeNbt(tag)

		renderLinksToAdd.clear()
		renderLinksToRemove.clear()
	}

	override fun applySyncPacket(buf: FriendlyByteBuf) {
		HexalAPI.LOGGER.info("attempting to apply synced packet to CCPlayerLinkstore for $player")
		val tag = buf.readNbt() ?: return

		val linksToAdd = (tag.get(TAG_RENDER_LINKS_ADD) as? ListTag)?.toIRenderCentreList(player.level as ClientLevel) ?: listOf()
		val linksToRemove = (tag.get(TAG_RENDER_LINKS_REMOVE) as? ListTag)?.toCompoundTagList()

		renderLinks.addAll(linksToAdd)
		if (linksToRemove != null)
			renderLinks.removeIf { currLink -> linksToRemove.any { LinkableRegistry.matchSync(currLink, it) } }
	}

	companion object {
		const val TAG_RENDER_LINKS_ADD = "render_links_add"
		const val TAG_RENDER_LINKS_REMOVE = "render_links_remove"
	}
}