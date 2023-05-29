package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.*
import ram.talia.hexal.common.lib.HexalBlockEntities
import kotlin.math.min

class BlockEntityRelay(pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.RELAY, pos, state), ILinkable, ILinkable.IRenderCentre {
    val pos: BlockPos = pos.immutable()

    private val random = RandomSource.create()

    private var relayNetwork: MutableSet<BlockEntityRelay> = mutableSetOf(this)
    private var directlyLinkedRelays: MutableSet<SerialisedBlockEntityRelay> = mutableSetOf()
    private var numNonRelaysLinked = 0
    private var numMediaAcceptorsLinked = 0
    private var lastTickComputedAverageMedia = 0L
    private var computedAverageMedia = 0 // average media among all ILinkables connected to the relay network.
        /**
         * sets computedAverageMedia to the average amount of media among all [ILinkable]s connected to the relay network, if it hasn't been called before this tick.
         */
        get() {
            if (lastTickComputedAverageMedia >= (level?.gameTime ?: 0))
                return field
            if (numMediaAcceptorsLinked == 0) {
                field = 0
                return 0
            }

            field = mediaExchangersLinked.fold(0) { c, m -> c + m.currentMediaLevel() } / numMediaAcceptorsLinked
            return field
        }

    private val nonRelaysLinkedDirectly: MutableSet<ILinkable> = mutableSetOf()
    private var nonRelaysLinked: MutableSet<ILinkable> = mutableSetOf()
    private var mediaExchangersLinkedDirectly: MutableSet<ILinkable> = mutableSetOf()
    private var mediaExchangersLinked: MutableSet<ILinkable> = mutableSetOf()

    private var timeColouriserSet = 0L
    private var colouriser: FrozenColorizer = FrozenColorizer(HexItems.DYE_COLORIZERS[DyeColor.PURPLE]?.let { ItemStack(it) }, Util.NIL_UUID)

    fun setColouriser(colorizer: FrozenColorizer, level: Level) {
        relayNetwork.forEach { it.colouriser = colorizer; it.timeColouriserSet = level.gameTime; it.sync() }
    }

    fun serverTick() {
        checkLinks()
    }

    fun clientTick() {
        renderLinks()
    }

    fun debug() {
        HexalAPI.LOGGER.info("relay network: $relayNetwork")
        HexalAPI.LOGGER.info("non relays linked: $nonRelaysLinked")
    }

    private fun combineNetworks(other: BlockEntityRelay) {
        if (this.timeColouriserSet > other.timeColouriserSet)
            other.relayNetwork.forEach { it.setColouriser(this.colouriser, level!!) }
        else
            this.relayNetwork.forEach { it.setColouriser(other.colouriser, level!!) }

        this.relayNetwork.addAll(other.relayNetwork)
        this.directlyLinkedRelays.add(other.toSerWrap())
        this.nonRelaysLinked.addAll(other.nonRelaysLinked)
        this.mediaExchangersLinked.addAll(other.mediaExchangersLinked)
        other.relayNetwork = this.relayNetwork
        other.directlyLinkedRelays.add(this.toSerWrap())
        other.nonRelaysLinked = this.nonRelaysLinked
        other.mediaExchangersLinked = this.mediaExchangersLinked
    }

    /**
     * Uses a depth first search to find all relays still connected to this. Any that are no longer connected to this
     * must be connected to other.
     */
    private fun findSeparateNetworks(other: BlockEntityRelay) {
        val unvisited = this.relayNetwork.toMutableSet()
        unvisited.remove(this)

        val newNetwork = mutableSetOf(this)
        // only add relays that are currently loaded to the frontier
        val frontier = this.directlyLinkedRelays.mapNotNull { this.level?.let { level -> it.getRelay(level) } }.toMutableList()

        while (frontier.isNotEmpty()) {
            val next = frontier.removeFirst()
            if (next in newNetwork)
                continue
            if (next == other) // if we've found an alternate path from this to other, clearly their networks are still connected
                return
            newNetwork.add(next)
            unvisited.remove(next)
            // only add relays that are currently loaded to the frontier
            frontier.addAll(next.directlyLinkedRelays.mapNotNull { this.level?.let { level -> it.getRelay(level) } }.filter { it !in newNetwork })
        }

        // separate out the non-relays that are connected to this's new network vs other's new network
        val (newNonRelaysLinked, newMediaExchangersLinked) = newNetwork.fold(mutableSetOf<ILinkable>() to mutableSetOf<ILinkable>())
            { (nonRelays, mediaExchangers), relay ->
                nonRelays.addAll(relay.nonRelaysLinkedDirectly); mediaExchangers.addAll(relay.mediaExchangersLinkedDirectly)
                nonRelays to mediaExchangers
            }
        val (unvisitedNonRelaysLinked, unvisitedMediaExchangersLinked) = unvisited.fold(mutableSetOf<ILinkable>() to mutableSetOf<ILinkable>())
        { (nonRelays, mediaExchangers), relay ->
            nonRelays.addAll(relay.nonRelaysLinkedDirectly); mediaExchangers.addAll(relay.mediaExchangersLinkedDirectly)
            nonRelays to mediaExchangers
        }

        // assign everything in the new network to contain the correct sets of things in and adjacent to the network. Do likewise for other's new network.
        newNetwork.forEach { it.relayNetwork = newNetwork; it.nonRelaysLinked = newNonRelaysLinked; it.mediaExchangersLinked = newMediaExchangersLinked }
        unvisited.forEach { it.relayNetwork = unvisited; it.nonRelaysLinked = unvisitedNonRelaysLinked; it.mediaExchangersLinked = unvisitedMediaExchangersLinked }
    }

    //region Linkable

    override val asActionResult: List<Iota>
        get() = pos.asActionResult

    private var cachedLinkableHolder: ServerLinkableHolder? = null
    private var serialisedLinkableHolder: CompoundTag? = null

    override val linkableHolder: ServerLinkableHolder?
        get() = cachedLinkableHolder ?: let {
                cachedLinkableHolder = (this.level as? ServerLevel)?.let { ServerLinkableHolder(this, it) }
                serialisedLinkableHolder?.let { cachedLinkableHolder?.readFromNbt(it)?.let { serialisedLinkableHolder = null } }
                cachedLinkableHolder
            }

    override fun getLinkableType() = LinkableTypes.RELAY_TYPE

    override fun getPosition(): Vec3 = Vec3.atCenterOf(pos)

    override fun maxSqrLinkRange(): Double = MAX_SQR_LINK_RANGE

    override fun shouldRemove(): Boolean = this.isRemoved

    override fun currentMediaLevel(): Int = computedAverageMedia

    override fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int {
        if (numMediaAcceptorsLinked - 1 == 0)
            return 0
        val averageMediaWithoutOther = (computedAverageMedia * numMediaAcceptorsLinked - otherMediaLevel) / (numMediaAcceptorsLinked - 1)

        if (otherMediaLevel <= averageMediaWithoutOther)
            return 0

        return ((otherMediaLevel - averageMediaWithoutOther) * HexalConfig.server.mediaFlowRateOverLink).toInt()
    }

    override fun acceptMedia(other: ILinkable, sentMedia: Int) {
        // TODO: handle same linkable connected to relay network multiple times
        var remainingMedia = sentMedia
        for (mediaAcceptor in mediaExchangersLinked.shuffled()) {
            if (other == mediaAcceptor)
                continue

            val toSend = mediaAcceptor.canAcceptMedia(this, computedAverageMedia)
            mediaAcceptor.acceptMedia(this, min(toSend, remainingMedia))
            remainingMedia -= min(toSend, remainingMedia)
            if (remainingMedia <= 0)
                break
        }
    }

    override fun link(other: ILinkable, linkOther: Boolean) {
        super.link(other, linkOther)

        if (other is BlockEntityRelay) {
            // don't need to run this on both relays
            if (!linkOther)
                return
            // combine the networks of other and this.
            combineNetworks(other)
        } else {
            // add to list of non-relays linked to network.
            nonRelaysLinkedDirectly.add(other)
            relayNetwork.forEach { it.nonRelaysLinked.add(other); it.numNonRelaysLinked += 1 }
            if (other.currentMediaLevel() != -1) {
                mediaExchangersLinked.add(other)
                relayNetwork.forEach { it.mediaExchangersLinked.add(other); it.numMediaAcceptorsLinked += 1 }
            }
        }
    }

    override fun unlink(other: ILinkable, unlinkOther: Boolean) {
        super.unlink(other, unlinkOther)

        if (other is BlockEntityRelay) {
            // don't need to run this on both relays
            if (!unlinkOther)
                return
            this.directlyLinkedRelays.remove(other.toSerWrap())
            other.directlyLinkedRelays.remove(this.toSerWrap())
            // uncombine the networks of other and this.
            findSeparateNetworks(other)
        } else {
            // remove from list of non-relays linked to network.
            nonRelaysLinkedDirectly.remove(other)
            relayNetwork.forEach { if (it.nonRelaysLinked.remove(other)) it.numNonRelaysLinked -= 1 }
            if (other.currentMediaLevel() != -1) {
                mediaExchangersLinked.remove(other)
                relayNetwork.forEach { if (it.mediaExchangersLinked.add(other)) it.numMediaAcceptorsLinked -= 1 }
            }
        }
    }

    override fun receiveIota(sender: ILinkable, iota: Iota) {
        nonRelaysLinked.forEach { if (it != sender) it.receiveIota(sender, iota) }
    }

    //endregion

    //region Linkable.IRenderCentre
    private var cachedClientLinkableHolder: ClientLinkableHolder? = null

    override val clientLinkableHolder: ClientLinkableHolder?
        get() = cachedClientLinkableHolder ?: let {
            cachedClientLinkableHolder = this.level?.let { if (it.isClientSide) ClientLinkableHolder(this, it, random) else null }
            cachedClientLinkableHolder
        }

    override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 {
        return Vec3.atCenterOf(pos) // TODO: Make this better; blockstates, tower, ect.
    }

    override fun colouriser(): FrozenColorizer {
        return colouriser // TODO: Make this better!
    }

    //endregion

    override fun loadModData(tag: CompoundTag) {
        if (tag.contains(TAG_COLOURISER))
            colouriser = FrozenColorizer.fromNBT(tag.getCompound(TAG_COLOURISER))
        if (tag.contains(TAG_COLOURISER_TIME))
            timeColouriserSet = tag.getLong(TAG_COLOURISER_TIME)
        if (tag.contains(TAG_LINKABLE_HOLDER))
            serialisedLinkableHolder = tag.getCompound(TAG_LINKABLE_HOLDER)
        if (tag.contains(TAG_DIRECTLY_LINKED_RELAYS))
            directlyLinkedRelaysFromTag(tag.getList(TAG_DIRECTLY_LINKED_RELAYS, ListTag.TAG_LIST))
    }

    override fun saveModData(tag: CompoundTag) {
        tag.put(TAG_COLOURISER, colouriser.serializeToNBT())
        tag.putLong(TAG_COLOURISER_TIME, timeColouriserSet)
        tag.putCompound(TAG_LINKABLE_HOLDER, linkableHolder!!.writeToNbt())
        tag.putList(TAG_DIRECTLY_LINKED_RELAYS, directlyLinkedRelaysToTag())
    }

    private fun directlyLinkedRelaysToTag(): ListTag {
        val out = ListTag()
        directlyLinkedRelays.forEach { out.add(blockPosToListTag(it.pos)) }
        return out
    }

    private fun directlyLinkedRelaysFromTag(tag: ListTag) {
        tag.forEach {
            directlyLinkedRelays.add(SerialisedBlockEntityRelay(listTagToBlockPos(it as ListTag)))
        }
    }

    private fun toSerWrap(): SerialisedBlockEntityRelay = SerialisedBlockEntityRelay(this)

    private data class SerialisedBlockEntityRelay(val pos: BlockPos) {
        constructor(relay: BlockEntityRelay) : this(relay.pos) {
            this.relay = relay
        }

        private var relay: BlockEntityRelay? = null

        fun getRelay(level: Level?): BlockEntityRelay? = relay ?: let {
            relay = level?.getBlockEntity(pos) as? BlockEntityRelay
            relay
        }
    }

    companion object {
        const val MAX_SQR_LINK_RANGE = 32.0*32.0

        const val TAG_COLOURISER = "hexal:colouriser"
        const val TAG_COLOURISER_TIME = "hexal:colouriser_time"
        const val TAG_LINKABLE_HOLDER = "hexal:linkable_holder"
        const val TAG_DIRECTLY_LINKED_RELAYS = "hexal:directly_linked_relays"

        private fun blockPosToListTag(pos: BlockPos): ListTag {
            val listTag = ListTag()
            listTag.add(IntTag.valueOf(pos.x))
            listTag.add(IntTag.valueOf(pos.y))
            listTag.add(IntTag.valueOf(pos.z))
            return listTag
        }

        private fun listTagToBlockPos(listTag: ListTag): BlockPos {
            val x = listTag.getInt(0)
            val y = listTag.getInt(1)
            val z = listTag.getInt(2)
            return BlockPos(x, y, z)
        }
    }
}