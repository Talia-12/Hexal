package ram.talia.hexal.api.linkable

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable.LazyILinkableList
import ram.talia.hexal.api.nbt.SerialisedIotaList

class LinkableHolder(private val thisLinkable: ILinkable, private val level: Level) {
    private var linked: MutableList<ILinkable>
        get() {
            if (level.isClientSide)
                throw Exception("LinkableEntity.linked should only be accessed on server.") // TODO: create and replace with ServerOnlyException
            return lazyLinked!!.get()
        }
        set(value) {
            lazyLinked?.set(value)
        }

    private val lazyLinked = if (level.isClientSide) null else LazyILinkableList(level as ServerLevel)

    val lazyRenderLinks = if (level.isClientSide) null else LazyILinkableList(level as ServerLevel)

    /**
     * Initialise to [SerialisedIotaList] (null)
     */
    val serReceivedIotas = SerialisedIotaList(null)

    private fun addRenderLink(other: ILinkable) {
        if (level.isClientSide)
            throw Exception("LinkableHolder.addRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException

        lazyRenderLinks!!.get().add(other)
//        syncAddRenderLink(other)
    }

    private fun removeRenderLink(other: ILinkable) {
        if (level.isClientSide)
            throw Exception("LinkableHolder.removeRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException
        lazyRenderLinks!!.get().remove(other)
//        syncRemoveRenderLink(other)
    }

    private fun removeRenderLink(index: Int) {
        if (level.isClientSide)
            throw Exception("LinkableHolder.removeRenderLink should only be accessed on server.") // TODO: create and replace with ServerOnlyException
        val other = lazyRenderLinks!!.get().removeAt(index)
//        syncRemoveRenderLink(other)
    }

    fun link(other: ILinkable, linkOther: Boolean = true) {
        if (level.isClientSide) {
            HexalAPI.LOGGER.info("$thisLinkable link called in a clientside context.")
            return
        }

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
        if (level.isClientSide) {
            HexalAPI.LOGGER.info("linkable $thisLinkable had unlink called in a clientside context.")
            return
        }

        HexalAPI.LOGGER.info("unlinking $thisLinkable from $other")

        linked.remove(other)
        removeRenderLink(other)

        if (unlinkOther) {
            other.unlink(thisLinkable, false)
        }
    }

    fun getLinked(index: Int): ILinkable {
        if (level.isClientSide)
            throw Exception("linkable $thisLinkable had getLinked called in a clientside context.") // TODO

        return linked[index]
    }

    fun getLinkedIndex(other: ILinkable): Int = linked.indexOf(other)

    fun numLinked(): Int = linked.size

    /**
     * This should be called every tick to remove links that should be removed (i.e. the entity that is linked to has been removed)
     */
    fun checkLinks() {
        for (i in (linked.size - 1) downTo 0) {
            if (linked[i].shouldRemove() || !thisLinkable.isInRange(linked[i]))
                unlink(linked[i])
        }
    }

    fun receiveIota(iota: Iota) {
        if (level.isClientSide)
            throw Exception("BaseWisp.receiveIota should only be called on server.") // TODO

        if (numRemainingIota() < ILinkable.MAX_RECEIVED_IOTAS)
            serReceivedIotas.add(iota, level as ServerLevel)
    }

    fun nextReceivedIota(): Iota {
        if (level.isClientSide)
            throw Exception("BaseWisp.receiveIota should only be called on server.") // TODO

        return serReceivedIotas.pop(level as ServerLevel) ?: NullIota()
    }

    fun numRemainingIota() = serReceivedIotas.size

    fun clearReceivedIotas() {
        serReceivedIotas.tag = ListTag()
    }
}