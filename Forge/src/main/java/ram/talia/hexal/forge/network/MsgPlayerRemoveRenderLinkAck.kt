package ram.talia.hexal.forge.network

import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler
import java.util.*

data class MsgPlayerRemoveRenderLinkAck(val playerUUID: UUID, val renderLinkTag: CompoundTag) : IMessage {
	override fun getFabricId() = ID

	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeUUID(playerUUID)

		val tag = CompoundTag()
		tag.put(TAG_RENDER_LINK, renderLinkTag)
		buf.writeNbt(tag)
	}

	companion object {
		val ID: ResourceLocation = modLoc("rrelink")

		const val TAG_RENDER_LINK = "render_link"

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgPlayerRemoveRenderLinkAck {
			val buf = FriendlyByteBuf(buffer)
			val uuid = buf.readUUID()
			val tag = buf.readNbt() ?: throw NullPointerException("no Nbt tag on received MsgPlayerAddRenderLinkAck")
			return MsgPlayerRemoveRenderLinkAck(
				uuid,
				tag.get(TAG_RENDER_LINK) as? CompoundTag ?: throw NullPointerException("no renderLinkTag on received MsgPlayerAddRenderLinkAck")
			)
		}

		@JvmStatic
		fun handle(self: MsgPlayerRemoveRenderLinkAck) {
			Minecraft.getInstance().execute {
				val mc = Minecraft.getInstance()

				if (mc.level == null)
					return@execute

				PlayerLinkstoreEventHandler.getRenderLinks(self.playerUUID).removeIf { it.equals(LinkableRegistry.fromSync(self.renderLinkTag, mc.level!!)) }
			}
		}
	}
}