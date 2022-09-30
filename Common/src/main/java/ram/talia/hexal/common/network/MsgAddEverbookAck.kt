package ram.talia.hexal.common.network

import at.petrak.hexcasting.common.network.IMessage
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI

data class MsgAddEverbookAck : IMessage {
	override fun serialize(buf: FriendlyByteBuf?) {
		TODO("Not yet implemented")
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("addever")
	}
}