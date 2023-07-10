package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.msgs.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.xplat.IClientXplatAbstractions

/**
 * Remove a pattern from the player's Everbook - should only be sent server to client.
 */
data class MsgRemoveEverbookS2C(val key: HexPattern) : IMessage {
	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeNbt(key.serializeToNBT())
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("remever")

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgRemoveEverbookS2C {
			val buf = FriendlyByteBuf(buffer)
			return MsgRemoveEverbookS2C(HexPattern.fromNBT(buf.readNbt()!!))
		}

		@JvmStatic
		fun handle(self: MsgRemoveEverbookS2C) {
			Minecraft.getInstance().execute {
				IClientXplatAbstractions.INSTANCE.removeClientEverbookIota(self.key)
			}
		}
	}
}