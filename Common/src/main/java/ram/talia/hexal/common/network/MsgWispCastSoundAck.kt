package ram.talia.hexal.common.network

import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.common.entities.BaseWisp

data class MsgWispCastSoundAck private constructor(val wispId: Int) : IMessage {
	constructor(wisp: BaseWisp) : this(wisp.id)

	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeInt(wispId)
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		public val ID: ResourceLocation = modLoc("wcstsnd")

		@JvmStatic
		fun deserialise(buffer: ByteBuf) = MsgWispCastSoundAck(buffer.readInt())

		@JvmStatic
		fun handle(self: MsgWispCastSoundAck) {
			Minecraft.getInstance().execute {
				val mc = Minecraft.getInstance()
				val level = mc.level ?: return@execute

				(level.getEntity(self.wispId) as BaseWisp).playCastSoundClient()
			}
		}
	}
}
