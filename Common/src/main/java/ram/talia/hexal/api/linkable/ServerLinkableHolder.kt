package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable.LazyILinkableList
import ram.talia.hexal.api.nbt.SerialisedIotaList
import ram.talia.hexal.xplat.IXplatAbstractions

class ServerLinkableHolder(private val thisLinkable: ILinkable, private val level: ServerLevel) {
    private var linked: MutableList<ILinkable>
        get() = lazyLinked.get()
        set(value) {
            lazyLinked.set(value)
        }

    private val lazyLinked = LazyILinkableList(level)

    private val lazyRenderLinks = LazyILinkableList(level)

    // used to sync full list of render links only on first tick.
    private var isFirstTick = true

    /**
     * Initialise to [SerialisedIotaList] (null)
     */
    private val serReceivedIotas = SerialisedIotaList()

    private fun addRenderLink(other: ILinkable) {
        lazyRenderLinks.get().add(other)
        IXplatAbstractions.INSTANCE.syncAddRenderLink(thisLinkable, other, level)
    }

    private fun removeRenderLink(other: ILinkable) {
        // only send a packet if something was actually removed.
        if (lazyRenderLinks.get().remove(other))
            IXplatAbstractions.INSTANCE.syncRemoveRenderLink(thisLinkable, other, level)
    }

    fun link(other: ILinkable, linkOther: Boolean = true) {
        if (other in linked || (other == thisLinkable))
            return

        HexalAPI.LOGGER.info("adding $other to $thisLinkable's links.")
        linked.add(other)

        if (linkOther) {
            HexalAPI.LOGGER.info("adding $other to $thisLinkable's render links.")
            addRenderLink(other)
        }

        if (linkOther) {
            other.link(thisLinkable, false)
        }
    }

    fun unlink(other: ILinkable, unlinkOther: Boolean = true) {
        HexalAPI.LOGGER.info("unlinking $thisLinkable from $other")

        linked.remove(other)
        removeRenderLink(other)

        if (unlinkOther) {
            other.unlink(thisLinkable, false)
        }
    }

    fun getLinked(index: Int): ILinkable {
        return linked[index]
    }

    fun getLinkedIndex(other: ILinkable): Int = linked.indexOf(other)

    fun numLinked(): Int = linked.size

    fun syncAll() {
        IXplatAbstractions.INSTANCE.syncSetRenderLinks(thisLinkable, lazyRenderLinks.get(), level)
    }

    /**
     * This should be called every tick to remove links that should be removed (i.e. the entity that is linked to has been removed)
     */
    fun checkLinks() {
        if (isFirstTick) {
            syncAll()
            isFirstTick = false
        }

        for (i in (linked.size - 1) downTo 0) {
            if (linked[i].shouldRemove() || !thisLinkable.isInRange(linked[i]))
                unlink(linked[i])
        }
    }

    fun receiveIota(iota: Iota) {
        if (numRemainingIota() < ILinkable.MAX_RECEIVED_IOTAS)
            serReceivedIotas.add(iota)
    }

    fun nextReceivedIota() = serReceivedIotas.pop(level) ?: NullIota()

    fun numRemainingIota() = serReceivedIotas.size()

    fun clearReceivedIotas() {
        serReceivedIotas.clear()
    }

    fun allReceivedIotas(): List<Iota> {
        return serReceivedIotas.getIotas(level)
    }

    /**
     * Call this when the ILinkable is being serialised, and save the resulting tag.
     */
    fun writeToNbt(): CompoundTag {
        val tag = CompoundTag()
        tag.put(TAG_LINKED, lazyLinked.getUnloaded())
        tag.put(TAG_RENDER_LINKED, lazyRenderLinks.getUnloaded())
        tag.put(TAG_RECEIVED, serReceivedIotas.getTag())
        return tag
    }

    /**
     * Call this when the ILinkable is being loaded, and pass the tag saved when [writeToNbt] was called.
     */
    fun readFromNbt(tag: CompoundTag) {
        when (val linkedTag = tag.get(TAG_LINKED) as? ListTag) {
            null -> lazyLinked.set(mutableListOf())
            else -> lazyLinked.set(linkedTag)
        }

        when (val renderLinkedTag = tag.get(TAG_RENDER_LINKED) as? ListTag) {
            null -> lazyRenderLinks.set(mutableListOf())
            else -> lazyRenderLinks.set(renderLinkedTag)
        }

        when (val receivedTag = tag.get(TAG_RECEIVED) as? ListTag) {
            null -> serReceivedIotas.clear()
            else -> serReceivedIotas.set(receivedTag)
        }
    }

    companion object {
        const val TAG_LINKED = "linked"
        const val TAG_RENDER_LINKED = "render_linked"
        const val TAG_RECEIVED = "received"
    }
}