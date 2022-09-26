package ram.talia.hexal.forge.network

import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.spell.toIRenderCentreList
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler
import java.util.*

data class MsgPlayerRenderLinksAck(val playerUUID: UUID, val renderLinksTag: ListTag) : IMessage {
	public val ID: ResourceLocation = modLoc("relinks")

	override fun getFabricId() = ID

	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeUUID(playerUUID)

		val tag = CompoundTag()
		tag.put(TAG_RENDER_LINK, renderLinksTag)
		buf.writeNbt(tag)
	}

	companion object {
		const val TAG_RENDER_LINK = "render_link"

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgPlayerRenderLinksAck {
			val buf = FriendlyByteBuf(buffer)
			val uuid = buf.readUUID()
			val tag = buf.readNbt() ?: throw NullPointerException("no Nbt tag on received MsgPlayerRenderLinksAck")
			return MsgPlayerRenderLinksAck(
				uuid,
				tag.get(TAG_RENDER_LINK) as? ListTag ?: throw NullPointerException("no renderLinkTag on received MsgPlayerRenderLinksAck")
			)
		}

		@JvmStatic
		fun handle(self: MsgPlayerRenderLinksAck) {
			Minecraft.getInstance().execute {
				val mc = Minecraft.getInstance()

				mc.level?.let { PlayerLinkstoreEventHandler.setRenderLinks(self.playerUUID, self.renderLinksTag.toIRenderCentreList(it)) }
			}
		}
	}
}