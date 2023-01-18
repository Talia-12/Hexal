package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.common.network.IMessage
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.nextColour

class MsgSingleParticleAck(val pos: Vec3, val colouriser: FrozenColorizer): IMessage {
    override fun serialize(buf: FriendlyByteBuf) {
        buf.writeDouble(pos.x)
        buf.writeDouble(pos.y)
        buf.writeDouble(pos.z)
        buf.writeNbt(colouriser.serializeToNBT())
    }

    override fun getFabricId() = ID

    companion object {
        @JvmField
        val ID: ResourceLocation = modLoc("sngprt")

        @JvmStatic
        fun deserialise(buffer: ByteBuf): MsgSingleParticleAck {
            val buf = FriendlyByteBuf(buffer)
            val pos = Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
            return MsgSingleParticleAck(pos, FrozenColorizer.fromNBT(buf.readNbt()!!))
        }

        @JvmStatic
        fun handle(self: MsgSingleParticleAck) {
            Minecraft.getInstance().execute {
                val level = Minecraft.getInstance().level ?: return@execute
                val colour = self.colouriser.nextColour(level.random)
                level.addParticle(ConjureParticleOptions(colour, false),
                        self.pos.x, self.pos.y, self.pos.z, 0.0, 0.0, 0.0)
                for (i in 0 .. 10) {
                    val colour = self.colouriser.nextColour(level.random)
                    val offsetX = level.random.nextFloat() * 0.1 - 0.05
                    val offsetY = level.random.nextFloat() * 0.1 - 0.05
                    val offsetZ = level.random.nextFloat() * 0.1 - 0.05
                    level.addParticle(ConjureParticleOptions(colour, false),
                            self.pos.x + offsetX, self.pos.y + offsetY, self.pos.z + offsetZ, 0.0, 0.0, 0.0)
                }
            }
        }
    }
}