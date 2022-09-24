package ram.talia.hexal.forge.network

import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler
import java.util.*

data class MsgPlayerClearRenderLinksAck(val playerUUID: UUID) : IMessage {
	public val ID: ResourceLocation = modLoc("crelinks")

	override fun getFabricId() = ID

	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeUUID(playerUUID)
	}

	companion object {
		@JvmStatic
		fun deserialize(buffer: ByteBuf): MsgPlayerClearRenderLinksAck {
			val buf = FriendlyByteBuf(buffer)
			return MsgPlayerClearRenderLinksAck(buf.readUUID())
		}

		@JvmStatic
		fun handle(self: MsgPlayerClearRenderLinksAck) {
			Minecraft.getInstance().execute {
				val mc = Minecraft.getInstance()

				if (mc.level == null)
					return@execute

				PlayerLinkstoreEventHandler.setRenderLinks(self.playerUUID, mutableListOf())
			}
		}
	}
}