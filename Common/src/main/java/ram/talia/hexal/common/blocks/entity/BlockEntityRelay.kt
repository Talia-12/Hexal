package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.*
import ram.talia.hexal.common.lib.HexalBlockEntities
import kotlin.math.min

class BlockEntityRelay(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.RELAY, pos, state), ILinkable, ILinkable.IRenderCentre {
    private val random = RandomSource.create()

    private var numNonRelaysLinked = 0
    private var numMediaAcceptorsLinked = 0
    private var lastTickComputedAverageMedia = 0L
    private var computedAverageMedia = 0 // average media among all ILinkables connected to the relay network.

    private val nonRelaysLinked: MutableList<ILinkable> = mutableListOf()
    private val mediaAcceptorsLinked: MutableList<ILinkable> = mutableListOf()

    /**
     * Only called once per tick per Relay network, sets computedAverageMedia to the average amount of media among all [ILinkable]s connected to the relay network.
     */
    fun computeAverageMedia() {

    }

    //region Linkable

    override val asActionResult: List<Iota>
        get() = pos.asActionResult

    private var cachedLinkableHolder: ServerLinkableHolder? = null

    override val linkableHolder: ServerLinkableHolder?
        get() = cachedLinkableHolder ?: let {
                cachedLinkableHolder = (this.level as? ServerLevel)?.let { ServerLinkableHolder(this, it) }
                cachedLinkableHolder
            }

    override fun getLinkableType() = LinkableTypes.RELAY_TYPE

    override fun getPosition(): Vec3 = Vec3.atCenterOf(pos)

    override fun maxSqrLinkRange(): Double = MAX_SQR_LINK_RANGE

    override fun shouldRemove(): Boolean = this.isRemoved

    override fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int {
        if (lastTickComputedAverageMedia < (level?.gameTime ?: 0))
            computeAverageMedia()

        val averageMediaWithoutOther = (computedAverageMedia * numMediaAcceptorsLinked - otherMediaLevel) / (numMediaAcceptorsLinked - 1)

        if (otherMediaLevel <= averageMediaWithoutOther)
            return 0

        return ((otherMediaLevel - averageMediaWithoutOther) * HexalConfig.server.mediaFlowRateOverLink).toInt()
    }

    override fun acceptMedia(other: ILinkable, sentMedia: Int) {
        // TODO: handle same linkable connected to relay network multiple times
        var remainingMedia = sentMedia
        for (mediaAcceptor in mediaAcceptorsLinked.shuffled()) {
            if (other == mediaAcceptor)
                continue

            val toSend = mediaAcceptor.canAcceptMedia(this, computedAverageMedia)
            mediaAcceptor.acceptMedia(this, min(toSend, remainingMedia))
            remainingMedia -= min(toSend, remainingMedia)
            if (remainingMedia <= 0)
                break
        }
    }

    //endregion

    //region Linkable.IRenderCentre
    private var cachedClientLinkableHolder: ClientLinkableHolder? = null

    override val clientLinkableHolder: ClientLinkableHolder?
        get() = cachedClientLinkableHolder ?: let {
            cachedClientLinkableHolder = this.level?.let { if (it.isClientSide) ClientLinkableHolder(this, it, random) else null }
            cachedClientLinkableHolder
        }

    override fun renderLinks() {
        super.renderLinks()
    }

    override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 {
        return Vec3.atCenterOf(pos) // TODO: Make this better; blockstates, tower, ect.
    }

    override fun colouriser(): FrozenColorizer {
        return colouriser // TODO: Make this better!
    }

    //endregion

    override fun loadModData(tag: CompoundTag) {
//        TODO("Not yet implemented")
    }

    override fun saveModData(tag: CompoundTag) {
//        TODO("Not yet implemented")
    }

    companion object {
        const val MAX_SQR_LINK_RANGE = 32.0*32.0

        val colouriser = FrozenColorizer(HexItems.DYE_COLORIZERS[DyeColor.PURPLE]?.let { ItemStack(it) }, Util.NIL_UUID)
    }
}