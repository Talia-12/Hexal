package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.msgs.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.xplat.IClientXplatAbstractions

class MsgToggleMacroS2C(val key: HexPattern) : IMessage {
	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeNbt(key.serializeToNBT())
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("togmac")

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgToggleMacroS2C {
			val buf = FriendlyByteBuf(buffer)
			return MsgToggleMacroS2C(HexPattern.fromNBT(buf.readNbt()!!))
		}

		@JvmStatic
		fun handle(self: MsgToggleMacroS2C) {
			Minecraft.getInstance().execute {
				IClientXplatAbstractions.INSTANCE.toggleClientEverbookMacro(self.key)
			}
		}
	}
}