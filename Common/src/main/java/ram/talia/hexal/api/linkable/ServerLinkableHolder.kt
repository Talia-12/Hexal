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

    private val lazyLinked = LazyILinkableList()

    private val lazyRenderLinks = LazyILinkableList()

    // used to sync full list of render links only on first tick.
    private var isFirstTick = true

    /**
     * Initialise to [SerialisedIotaList] (null)
     */
    private val serReceivedIotas = SerialisedIotaList()

    private fun addRenderLink(other: ILinkable) {
        lazyRenderLinks.add(other)
        IXplatAbstractions.INSTANCE.syncAddRenderLink(thisLinkable, other, level)
    }

    private fun removeRenderLink(other: ILinkable) {
        // only send a packet if something was actually removed.
        if (lazyRenderLinks.remove(other))
            IXplatAbstractions.INSTANCE.syncRemoveRenderLink(thisLinkable, other, level)
    }

    fun link(other: ILinkable, linkOther: Boolean = true) {
        if (lazyLinked.contains(other) || (other == thisLinkable))
            return

        HexalAPI.LOGGER.debug("adding {} to {}'s links.", other, thisLinkable)
        lazyLinked.add(other)

        if (linkOther) {
            HexalAPI.LOGGER.debug("adding {} to {}'s render links.", other, thisLinkable)
            addRenderLink(other)
        }

        if (linkOther) {
            other.link(thisLinkable, false)
        }
    }

    fun unlink(other: ILinkable, unlinkOther: Boolean = true) {
        HexalAPI.LOGGER.debug("unlinking {} from {}", thisLinkable, other)

        lazyLinked.remove(other)
        removeRenderLink(other)

        if (unlinkOther) {
            other.unlink(thisLinkable, false)
        }
    }

    fun getLinked(index: Int): ILinkable? {
        return lazyLinked[index]
    }

    fun getLinkedIndex(other: ILinkable): Int = lazyLinked.indexOf(other)

    fun numLinked(): Int = lazyLinked.size()

    fun syncAll() {
        IXplatAbstractions.INSTANCE.syncSetRenderLinks(thisLinkable, lazyRenderLinks.getLoaded(), level)
    }

    /**
     * This should be called every tick to remove links that should be removed (i.e. the entity that is linked to has been removed)
     */
    fun checkLinks() {
        if (isFirstTick) {
            lazyLinked.tryLoad(level)
            lazyRenderLinks.tryLoad(level)

            syncAll()
            isFirstTick = false
        }

        // clear entities that have been removed from the world at least once per second
        // to prevent any memory leak type errors
        if (level.gameTime % 20 == 0L)
            serReceivedIotas.refreshIotas(level)

        if (level.gameTime % 20 == 10L) {
            lazyLinked.tryLoad(level)
            lazyRenderLinks.tryLoad(level).forEach { IXplatAbstractions.INSTANCE.syncAddRenderLink(thisLinkable, it, level) }
        }

        for (i in (lazyLinked.size() - 1) downTo 0) {
            val linked = lazyLinked[i] ?: continue

            if (linked.shouldRemove() || !thisLinkable.isInRange(linked))
                unlink(linked)
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