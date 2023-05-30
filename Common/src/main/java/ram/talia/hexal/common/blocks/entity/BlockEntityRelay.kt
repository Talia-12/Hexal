package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.utils.asCompound
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
import ram.talia.hexal.api.linkable.ILinkable.LazyILinkableSet
import ram.talia.hexal.api.plus
import ram.talia.hexal.common.lib.HexalBlockEntities
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory
import kotlin.math.min

class BlockEntityRelay(pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.RELAY, pos, state), ILinkable, ILinkable.IRenderCentre, IAnimatable {
    val pos: BlockPos = pos.immutable()

    private val random = RandomSource.create()

    private var relayNetwork: RelayNetwork = RelayNetwork(this, mutableSetOf(this), mutableSetOf(), mutableSetOf())
    private var relaysLinkedDirectly: MutableSet<SerialisedBlockEntityRelay> = mutableSetOf()


    private val nonRelaysLinkedDirectly: LazyILinkableSet = LazyILinkableSet()
    private var mediaExchangersLinkedDirectly: LazyILinkableSet = LazyILinkableSet()

    fun setColouriser(colorizer: FrozenColorizer, level: Level) = relayNetwork.setColouriser(colorizer, level.gameTime)

    fun serverTick() {
        checkLinks()

        nonRelaysLinkedDirectly.removeIf { it.shouldRemove() }
        mediaExchangersLinkedDirectly.removeIf { it.shouldRemove() }

        relayNetwork.tick()

        if (level != null && !level!!.isClientSide && level!!.gameTime % 20 == 0L) {
            relaysLinkedDirectly.filter { it.loadRelay(level) }.forEach { it.getRelay(level)?.let { it1 -> combineNetworks(it1) } }

            val newNonRelays = nonRelaysLinkedDirectly.tryLoad(level as ServerLevel)
            val newMediaExchangers = mediaExchangersLinkedDirectly.tryLoad(level as ServerLevel)
            relayNetwork.nonRelays.addAll(newNonRelays)
            relayNetwork.mediaExchangers.addAll(newMediaExchangers)
            relayNetwork.numNonRelays += newNonRelays.size
            relayNetwork.numMediaExchangers += newMediaExchangers.size
        }
    }

    fun clientTick() {
        renderLinks()
    }

    fun debug() {
        HexalAPI.LOGGER.info("relay network: $relayNetwork")
    }

    private fun combineNetworks(other: BlockEntityRelay) {
        if (this.relayNetwork == other.relayNetwork)
            return

        this.relayNetwork.absorb(other.relayNetwork)

        this.relaysLinkedDirectly.add(other.toSerWrap())
        other.relaysLinkedDirectly.add(this.toSerWrap())
    }

    /**
     * Uses a depth first search to find all relays still connected to this. Any that are no longer connected to this
     * must be connected to other.
     */
    private fun findSeparateNetworks(other: BlockEntityRelay) {
        val unvisited = this.relayNetwork.relays.toMutableSet()
        unvisited.remove(this)

        val visited = mutableSetOf(this)
        // only add relays that are currently loaded to the frontier
        val frontier = this.relaysLinkedDirectly.mapNotNull { this.level?.let { level -> it.getRelay(level) } }.toMutableList()

        while (frontier.isNotEmpty()) {
            val next = frontier.removeFirst()
            if (next in visited)
                continue
            if (next == other) // if we've found an alternate path from this to other, clearly their networks are still connected
                return
            visited.add(next)
            unvisited.remove(next)
            // only add relays that are currently loaded to the frontier
            frontier.addAll(next.relaysLinkedDirectly.mapNotNull { this.level?.let { level -> it.getRelay(level) } }.filter { it !in visited })
        }

        // separate out the non-relays that are connected to this's new network vs other's new network
        val thisNetwork = makeNetwork(this, visited)
        val otherNetwork = makeNetwork(other, unvisited)

        // assign everything in the new network to contain the correct sets of things in and adjacent to the network. Do likewise for other's new network.
        visited.forEach {
            it.relayNetwork = thisNetwork
        }
        unvisited.forEach {
            it.relayNetwork = otherNetwork
        }
    }

    private fun makeNetwork(root: BlockEntityRelay, relays: MutableSet<BlockEntityRelay>): RelayNetwork {
        val (newNonRelaysLinked, newMediaExchangersLinked) = relays.fold(mutableSetOf<ILinkable>() to mutableSetOf<ILinkable>())
        { (nonRelays, mediaExchangers), relay ->
            nonRelays.addAll(relay.nonRelaysLinkedDirectly)
            mediaExchangers.addAll(relay.mediaExchangersLinkedDirectly)
            nonRelays to mediaExchangers
        }

        val network = RelayNetwork(root, relays, newNonRelaysLinked, newMediaExchangersLinked)
        network.setColouriser(root.colouriser(), root.level?.gameTime ?: 0L)

        return network
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

    override fun currentMediaLevel(): Int = relayNetwork.computedAverageMedia

    override fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int = relayNetwork.canAcceptMedia(other, otherMediaLevel)

    override fun acceptMedia(other: ILinkable, sentMedia: Int) = relayNetwork.acceptMedia(other, sentMedia)

    override fun link(other: ILinkable, linkOther: Boolean) {
        super.link(other, linkOther)

        if (other is BlockEntityRelay) {
            // don't need to run this on both relays
            if (!linkOther)
                return
            // combine the networks of other and this.
            combineNetworks(other)
            this.setChanged()
            other.setChanged()
        } else {
            // add to list of non-relays linked to network.
            nonRelaysLinkedDirectly.add(other)
            relayNetwork.nonRelays.add(other)
            relayNetwork.numNonRelays += 1
            setChanged()
            if (other.currentMediaLevel() != -1) {
                mediaExchangersLinkedDirectly.add(other)
                relayNetwork.mediaExchangers.add(other)
                relayNetwork.numMediaExchangers += 1
            }
        }
    }

    override fun unlink(other: ILinkable, unlinkOther: Boolean) {
        super.unlink(other, unlinkOther)

        if (other is BlockEntityRelay) {
            // don't need to run this on both relays
            if (!unlinkOther)
                return
            this.relaysLinkedDirectly.remove(other.toSerWrap())
            other.relaysLinkedDirectly.remove(this.toSerWrap())
            // uncombine the networks of other and this.
            findSeparateNetworks(other)
            this.setChanged()
            other.setChanged()
        } else {
            // remove from list of non-relays linked to network.
            nonRelaysLinkedDirectly.remove(other)
            relayNetwork.nonRelays.remove(other)
            relayNetwork.numNonRelays -= 1
            setChanged()
            if (other.currentMediaLevel() != -1) {
                mediaExchangersLinkedDirectly.remove(other)
                relayNetwork.mediaExchangers.remove(other)
                relayNetwork.numMediaExchangers -= 1
            }
        }
    }

    fun disconnectAll() {
        for (relay in relaysLinkedDirectly)
            relay.getRelay(level)?.let { unlink(it) }
    }

    override fun receiveIota(sender: ILinkable, iota: Iota) {
        relayNetwork.nonRelays.forEach { if (it != sender) it.receiveIota(sender, iota) }
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
        return Vec3.atCenterOf(pos) + getBobberPosition() // TODO: Make this better; blockstates, tower, ect.
    }

    override fun colouriser(): FrozenColorizer = relayNetwork.colouriser

    //endregion

    //region IAnimatable
    @Suppress("DEPRECATION", "removal")
    private val factory: AnimationFactory = AnimationFactory(this)

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "controller", 0.0f, this::predicate))
    }

    private fun <E : IAnimatable> predicate(event: AnimationEvent<E>): PlayState {
        @Suppress("DEPRECATION", "removal")
        event.controller.setAnimation(
                AnimationBuilder()
                        .addAnimation("animation.model.place", false)
                        .addAnimation("animation.model.idle", true)
        )

        return PlayState.CONTINUE
    }

    override fun getFactory(): AnimationFactory = factory

    private fun getBobberPosition(): Vec3 {
        val manager = factory.getOrCreateAnimationData(this.pos.hashCode()) // this is how the unique ID is calculated in GeoBlockRenderer
        val bobber = manager.boneSnapshotCollection["Bobber"]?.right ?: return Vec3.ZERO
        return Vec3(bobber.positionOffsetX / 16.0, (bobber.positionOffsetY + 10) / 16.0, bobber.positionOffsetZ / 16.0)
    }
    //endregion

    override fun loadModData(tag: CompoundTag) {
        HexalAPI.LOGGER.info("loading $tag at $pos on $level")
        if (tag.contains(TAG_COLOURISER))
            relayNetwork.colouriser = FrozenColorizer.fromNBT(tag.getCompound(TAG_COLOURISER))
        if (tag.contains(TAG_COLOURISER_TIME))
            relayNetwork.timeColouriserSet = tag.getLong(TAG_COLOURISER_TIME)
        if (tag.contains(TAG_LINKABLE_HOLDER))
            serialisedLinkableHolder = tag.getCompound(TAG_LINKABLE_HOLDER)
        if (tag.contains(TAG_RELAYS_LINKED_DIRECTLY))
            relaysLinkedDirectlyFromTag(tag.getList(TAG_RELAYS_LINKED_DIRECTLY, ListTag.TAG_LIST))
        if (tag.contains(TAG_NON_RELAYS_LINKED_DIRECTLY))
            nonRelaysLinkedDirectlyFromTag(tag.getList(TAG_NON_RELAYS_LINKED_DIRECTLY, ListTag.TAG_COMPOUND))
    }

    override fun saveModData(tag: CompoundTag) {
        tag.put(TAG_COLOURISER, relayNetwork.colouriser.serializeToNBT())
        tag.putLong(TAG_COLOURISER_TIME, relayNetwork.timeColouriserSet)
        tag.putCompound(TAG_LINKABLE_HOLDER, linkableHolder!!.writeToNbt())
        tag.putList(TAG_RELAYS_LINKED_DIRECTLY, relaysDirectlyLinkedToTag())
        tag.putList(TAG_NON_RELAYS_LINKED_DIRECTLY, nonRelaysLinkedDirectlyToTag())
        HexalAPI.LOGGER.info("saving $tag at $pos on $level")
    }

    private fun relaysDirectlyLinkedToTag(): ListTag {
        val out = ListTag()
        relaysLinkedDirectly.forEach { out.add(blockPosToListTag(it.pos)) }
        return out
    }

    private fun relaysLinkedDirectlyFromTag(tag: ListTag) {
        tag.forEach {
            relaysLinkedDirectly.add(SerialisedBlockEntityRelay(listTagToBlockPos(it as ListTag)))
        }
    }

    private fun nonRelaysLinkedDirectlyToTag(): ListTag {
        val out = ListTag()
        val unloadedMediaExchangers = mediaExchangersLinkedDirectly.getLazies().map { it.getUnloaded() }
        nonRelaysLinkedDirectly.getLazies().map {
            val tag = it.getUnloaded()
            tag.putBoolean(TAG_MEDIA_EXCHANGER, tag in unloadedMediaExchangers)
            out.add(tag)
        }
        return out
    }

    private fun nonRelaysLinkedDirectlyFromTag(tag: ListTag) {
        nonRelaysLinkedDirectly.clear()
        mediaExchangersLinkedDirectly.clear()

        tag.forEach {
            val ctag = it.asCompound
            val lazy = ILinkable.LazyILinkable.from(ctag)
            if (ctag.getBoolean(TAG_MEDIA_EXCHANGER))
                mediaExchangersLinkedDirectly.add(lazy)
            nonRelaysLinkedDirectly.add(lazy)
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

        fun loadRelay(level: Level?): Boolean = if (relay != null) false else getRelay(level) != null
    }

    private data class RelayNetwork(val root: BlockEntityRelay, val relays: MutableSet<BlockEntityRelay>, val nonRelays: MutableSet<ILinkable>, val mediaExchangers: MutableSet<ILinkable>) {
        var numNonRelays = nonRelays.size
        var numMediaExchangers = mediaExchangers.size
        var lastTickComputedAverageMedia = 0L
        var computedAverageMedia = 0 // average media among all ILinkables connected to the relay network.
            /**
             * sets computedAverageMedia to the average amount of media among all [ILinkable]s connected to the relay network, if it hasn't been called before this tick.
             */
            get() {
                if (lastTickComputedAverageMedia >= (root.level?.gameTime ?: 0))
                    return field
                if (numMediaExchangers == 0) {
                    field = 0
                    return 0
                }

                field = mediaExchangers.fold(0) { c, m -> c + m.currentMediaLevel() } / numMediaExchangers
                return field
            }

        var timeColouriserSet = 0L
        var colouriser: FrozenColorizer = FrozenColorizer(HexItems.DYE_COLORIZERS[DyeColor.PURPLE]?.let { ItemStack(it) }, Util.NIL_UUID)

        fun setColouriser(colorizer: FrozenColorizer, time: Long) {
            colouriser = colorizer
            timeColouriserSet = time
            root.sync()
        }

        var lastTickAcceptedMedia = 0L
        val linkablesAcceptedFromThisTick: MutableSet<ILinkable> = mutableSetOf()

        fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int {
            if (lastTickAcceptedMedia < (root.level?.gameTime ?: 0L)) {
                linkablesAcceptedFromThisTick.clear()
                lastTickAcceptedMedia = root.level?.gameTime ?: 0L
            }
            if (other in linkablesAcceptedFromThisTick)
                return 0

            if (numMediaExchangers - 1 == 0)
                return 0
            val averageMediaWithoutOther = (computedAverageMedia * numMediaExchangers - otherMediaLevel) / (numMediaExchangers - 1)

            if (otherMediaLevel <= averageMediaWithoutOther)
                return 0

            return ((otherMediaLevel - averageMediaWithoutOther) * HexalConfig.server.mediaFlowRateOverLink).toInt()
        }

        fun acceptMedia(other: ILinkable, sentMedia: Int) {
            var remainingMedia = sentMedia
            for (mediaAcceptor in mediaExchangers.shuffled()) {
                if (other == mediaAcceptor)
                    continue

                val toSend = mediaAcceptor.canAcceptMedia(root, computedAverageMedia)
                mediaAcceptor.acceptMedia(root, min(toSend, remainingMedia))
                remainingMedia -= min(toSend, remainingMedia)
                if (remainingMedia <= 0)
                    break
            }

            linkablesAcceptedFromThisTick.add(other)
        }

        fun absorb(other: RelayNetwork) {
            if (this.timeColouriserSet < other.timeColouriserSet) {
                this.colouriser = other.colouriser
                this.timeColouriserSet = other.timeColouriserSet
            }

            this.relays.addAll(other.relays)
            this.nonRelays.addAll(other.nonRelays)
            this.mediaExchangers.addAll(other.mediaExchangers)
            this.numNonRelays += other.numNonRelays
            this.numMediaExchangers += other.numMediaExchangers

            other.relays.forEach { it.relayNetwork = this }
        }

        fun tick() {
            nonRelays.removeIf { it.shouldRemove().also { if (it) numNonRelays -= 1 } }
            mediaExchangers.removeIf { it.shouldRemove().also { if (it) numMediaExchangers -= 1 } }
        }
    }

    companion object {
        const val MAX_SQR_LINK_RANGE = 32.0*32.0

        const val TAG_COLOURISER = "hexal:colouriser"
        const val TAG_COLOURISER_TIME = "hexal:colouriser_time"
        const val TAG_LINKABLE_HOLDER = "hexal:linkable_holder"
        const val TAG_RELAYS_LINKED_DIRECTLY = "hexal:relays_linked_directly"
        const val TAG_NON_RELAYS_LINKED_DIRECTLY = "hexal:non_relays_linked_directly"
        const val TAG_MEDIA_EXCHANGER = "relay:media_exchanger"

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