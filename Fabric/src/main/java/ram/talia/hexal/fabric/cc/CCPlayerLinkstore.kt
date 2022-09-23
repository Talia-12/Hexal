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
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.PlayerLinkstore
import ram.talia.hexal.api.spell.toIRenderCentreList
import ram.talia.hexal.api.spell.toSyncTag

public class CCPlayerLinkstore(private val owner: Player) : ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {
	val linkstore = (owner as? ServerPlayer)?.let { PlayerLinkstore(it) }
	var renderLinks: List<ILinkable.IRenderCentre> = listOf()

	override fun serverTick() = linkstore?.pruneLinks() ?: Unit

	override fun clientTick() {
		TODO("Not yet implemented - render links") // probably need to split the renderLinks() function out into a more accessible place.
		// also need to sync the render links out of linkstore somehow??
	}

	override fun readFromNbt(tag: CompoundTag) {
		linkstore?.loadAdditionalData(tag)
	}

	override fun writeToNbt(tag: CompoundTag) {
		linkstore?.saveAdditionalData(tag)
	}

	override fun writeSyncPacket(buf: FriendlyByteBuf, recipient: ServerPlayer) {
		val tag = CompoundTag()
		tag.put(TAG_RENDER_LINKS, linkstore!!.renderLinks.toSyncTag())
		buf.writeNbt(tag)
	}

	override fun applySyncPacket(buf: FriendlyByteBuf) {
		val tag = buf.readNbt() ?: return

		renderLinks = (tag.get(TAG_RENDER_LINKS) as? ListTag)?.toIRenderCentreList(owner.level as ClientLevel) ?: listOf()
	}

	companion object {
		const val TAG_RENDER_LINKS = "render_links"
	}
}