package ram.talia.hexal.common.network

import at.petrak.hexcasting.common.network.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry

class MsgAddRenderLinkAck(val sourceLinkTag: CompoundTag, val sinkLinkTag: CompoundTag) : IMessage {
    constructor(sourceLink: ILinkable, sinkLink: ILinkable) : this(LinkableRegistry.wrapSync(sourceLink), LinkableRegistry.wrapSync(sinkLink))

    override fun getFabricId() = ID

    override fun serialize(buf: FriendlyByteBuf) {
        val tag = CompoundTag()
        tag.put(TAG_SOURCE_LINK, sourceLinkTag)
        tag.put(TAG_SINK_LINK, sinkLinkTag)
        buf.writeNbt(tag)
    }

    companion object {
        @JvmField
        val ID: ResourceLocation = HexalAPI.modLoc("ralink")

        const val TAG_SOURCE_LINK = "source"
        const val TAG_SINK_LINK = "sink"

        @JvmStatic
        fun deserialise(buffer: ByteBuf): MsgAddRenderLinkAck {
            val buf = FriendlyByteBuf(buffer)
            val tag = buf.readNbt() ?: throw NullPointerException("no Nbt tag on received MsgPlayerAddRenderLinkAck")
            return MsgAddRenderLinkAck(
                    tag.get(TAG_SOURCE_LINK) as? CompoundTag
                            ?: throw NullPointerException("no sourceLinkTag on received MsgPlayerAddRenderLinkAck"),

                    tag.get(TAG_SINK_LINK) as? CompoundTag
                            ?: throw NullPointerException("no sinkLinkTag on received MsgPlayerAddRenderLinkAck")
            )
        }

        @JvmStatic
        fun handle(self: MsgAddRenderLinkAck) {
            Minecraft.getInstance().execute {
                val mc = Minecraft.getInstance()

                if (mc.level == null)
                    return@execute

                // add the sink to the source's list of IRenderCentres only if both are non-null.
                LinkableRegistry.fromSync(self.sinkLinkTag, mc.level!!)?.let {
                    LinkableRegistry.fromSync(self.sourceLinkTag, mc.level!!)?.clientLinkableHolder?.addRenderLink(self.sinkLinkTag, it)
                }
            }
        }
    }
}