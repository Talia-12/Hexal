package ram.talia.hexal.fabric.cc

import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.PlayerLinkstore
import ram.talia.hexal.api.spell.toIRenderCentreList
import ram.talia.hexal.api.spell.toSyncTag
import ram.talia.hexal.client.playLinkParticles

public class CCPlayerLinkstore(private val player: Player) : ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {
	val linkstore = (player as? ServerPlayer)?.let { PlayerLinkstore(it) }
	val ownerRenderCentre = (player as? AbstractClientPlayer)?.let{ PlayerLinkstore.RenderCentre(it) }
	var renderLinks: List<ILinkable.IRenderCentre> = listOf()

	override fun serverTick() {
		linkstore!!.pruneLinks()
		HexalCardinalComponents.PLAYER_LINKSTORE.sync(player)
	}

	override fun clientTick()
		= renderLinks.forEach { playLinkParticles(ownerRenderCentre!!, it, IXplatAbstractions.INSTANCE.getColorizer(player), player.random, player.level) }

	override fun readFromNbt(tag: CompoundTag) {
		linkstore?.loadAdditionalData(tag)
	}

	override fun writeToNbt(tag: CompoundTag) {
		linkstore?.saveAdditionalData(tag)
	}

	override fun writeSyncPacket(buf: FriendlyByteBuf, recipient: ServerPlayer) {
		HexalAPI.LOGGER.info("attempting to sync CCPlayerLinkstore for $player")
		val tag = CompoundTag()
		tag.put(TAG_RENDER_LINKS, linkstore!!.renderLinks.toSyncTag())
		buf.writeNbt(tag)
	}

	override fun applySyncPacket(buf: FriendlyByteBuf) {
		HexalAPI.LOGGER.info("attempting to apply synced packet to CCPlayerLinkstore for $player")
		val tag = buf.readNbt() ?: return

		renderLinks = (tag.get(TAG_RENDER_LINKS) as? ListTag)?.toIRenderCentreList(player.level as ClientLevel) ?: listOf()
	}

	companion object {
		const val TAG_RENDER_LINKS = "render_links"
	}
}