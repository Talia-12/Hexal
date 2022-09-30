package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.xplat.IClientXplatAbstractions

data class MsgSetEverbookAck(val key: HexPattern, val iota: CompoundTag) : IMessage {
	override fun serialize(buf: FriendlyByteBuf) {
		buf.writeNbt(key.serializeToNBT())
		buf.writeNbt(iota)
	}

	override fun getFabricId() = ID

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("setever")

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgSetEverbookAck {
			val buf = FriendlyByteBuf(buffer)
			return MsgSetEverbookAck(HexPattern.fromNBT(buf.readNbt()!!), buf.readNbt()!!)
		}

		@JvmStatic
		fun handle(self: MsgSetEverbookAck) {
			Minecraft.getInstance().execute {
				IClientXplatAbstractions.INSTANCE.setClientEverbookIota(self.key, self.iota)
			}
		}
	}
}