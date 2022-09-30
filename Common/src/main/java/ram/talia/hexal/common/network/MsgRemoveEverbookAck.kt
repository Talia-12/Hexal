package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.BaseWisp
import ram.talia.hexal.xplat.IXplatAbstractions

/**
 * Remove a pattern from the player's Everbook - should only be sent server to client.
 */
data class MsgRemoveEverbookAck(val key: HexPattern) : IMessage {
	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeNbt(key.serializeToNBT())
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("remever")

		@JvmStatic
		fun deserialise(buffer: ByteBuf) {
			val buf = FriendlyByteBuf(buffer)
			MsgRemoveEverbookAck(HexPattern.fromNBT(buf.readNbt()!!))
		}

		@JvmStatic
		fun handle(self: MsgWispCastSoundAck) {
			Minecraft.getInstance().execute {
				IXplatAbstractions.INSTANCE.rem
			}
		}
	}
}