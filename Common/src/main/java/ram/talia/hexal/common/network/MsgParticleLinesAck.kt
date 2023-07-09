package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.msgs.IMessage
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nextColour
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.times

class MsgParticleLinesAck(val locs: List<Vec3>, val colouriser: FrozenPigment): IMessage {
    override fun serialize(buf: FriendlyByteBuf) {
        buf.writeInt(locs.size)
        for (loc in locs) {
            buf.writeDouble(loc.x)
            buf.writeDouble(loc.y)
            buf.writeDouble(loc.z)
        }
        buf.writeNbt(colouriser.serializeToNBT())
    }

    override fun getFabricId() = ID

    companion object {
        @JvmField
        val ID: ResourceLocation = modLoc("prtlns")

        @JvmStatic
        fun deserialise(buffer: ByteBuf): MsgParticleLinesAck {
            val buf = FriendlyByteBuf(buffer)
            val numLocs = buf.readInt()
            val locs = mutableListOf<Vec3>()

            for (i in 1 .. numLocs) {
                locs.add(Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()))
            }

            return MsgParticleLinesAck(locs, FrozenPigment.fromNBT(buf.readNbt()!!))
        }

        @JvmStatic
        fun handle(self: MsgParticleLinesAck) {
            Minecraft.getInstance().execute {
                val level = Minecraft.getInstance().level ?: return@execute

                self.locs.zipWithNext { start, end ->
                    val steps = ((end - start).length() * 10).toInt()
                    for (i in 0 .. steps) {
                        val pos = start + (i.toDouble() / steps) * (end - start)
                        val colour = self.colouriser.nextColour(level.random)
                        level.addParticle(ConjureParticleOptions(colour),
                                pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
                    }
                }
            }
        }
    }
}