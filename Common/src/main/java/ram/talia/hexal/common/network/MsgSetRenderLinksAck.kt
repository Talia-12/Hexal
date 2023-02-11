package ram.talia.hexal.common.network

import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.nbt.toIRenderCentreList
import ram.talia.hexal.api.nbt.toSyncTag
import ram.talia.hexal.client.LinkablePacketHolder

class MsgSetRenderLinksAck(val sourceLinkTag: CompoundTag, val sinksTag: ListTag) : IMessage {

    constructor(sourceLink: ILinkable, sinks: List<ILinkable>)
            : this(LinkableRegistry.wrapSync(sourceLink), sinks.toSyncTag())


    override fun getFabricId() = ID

    override fun serialize(buf: FriendlyByteBuf) {
        val tag = CompoundTag()
        tag.put(TAG_SOURCE_LINK, sourceLinkTag)
        tag.put(TAG_SINKS, sinksTag)
        buf.writeNbt(tag)
    }

    companion object {
        @JvmField
        val ID: ResourceLocation = HexalAPI.modLoc("rslinks")

        const val TAG_SOURCE_LINK = "source"
        const val TAG_SINKS = "sinks"

        @JvmStatic
        fun deserialise(buffer: ByteBuf): MsgSetRenderLinksAck {
            val buf = FriendlyByteBuf(buffer)
            val tag = buf.readNbt() ?: throw NullPointerException("no Nbt tag on received MsgSetRenderLinksAck")
            return MsgSetRenderLinksAck(
                    tag.get(TAG_SOURCE_LINK) as? CompoundTag
                            ?: throw NullPointerException("no sourceLinkTag on received MsgSetRenderLinksAck"),

                    tag.get(TAG_SINKS) as? ListTag
                            ?: throw NullPointerException("no sinks on received MsgSetRenderLinksAck")
            )
        }

        @JvmStatic
        fun handle(self: MsgSetRenderLinksAck) {
            Minecraft.getInstance().execute {
                val mc = Minecraft.getInstance()

                if (mc.level == null)
                    return@execute

                val sinks = self.sinksTag.toIRenderCentreList(mc.level!!)

                // if the source is null, schedule the packet to retry.
                LinkableRegistry.fromSync(self.sourceLinkTag, mc.level!!)?.clientLinkableHolder?.setRenderLinks(sinks) ?: LinkablePacketHolder.schedule(self)
            }
        }
    }
}