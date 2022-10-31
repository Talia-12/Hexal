package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.xplat.IClientXplatAbstractions

class MsgToggleMacroAck(val key: HexPattern) : IMessage {
	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeNbt(key.serializeToNBT())
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("togmac")

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgToggleMacroAck {
			val buf = FriendlyByteBuf(buffer)
			return MsgToggleMacroAck(HexPattern.fromNBT(buf.readNbt()!!))
		}

		@JvmStatic
		fun handle(self: MsgToggleMacroAck) {
			Minecraft.getInstance().execute {
				IClientXplatAbstractions.INSTANCE.toggleClientEverbookMacro(self.key)
			}
		}
	}
}