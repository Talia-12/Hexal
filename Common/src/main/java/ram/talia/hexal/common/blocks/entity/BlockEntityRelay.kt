package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.linkable.*
import ram.talia.hexal.common.lib.HexalBlockEntities

class BlockEntityRelay(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.RELAY, pos, state), ILinkable, ILinkable.IRenderCentre {

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
        TODO("Not yet implemented")
    }

    override fun acceptMedia(other: ILinkable, sentMedia: Int) {
        TODO("Not yet implemented")
    }

    //endregion

    //region Linkable.IRenderCentre

    override val clientLinkableHolder: ClientLinkableHolder?
        get() = TODO("Not yet implemented")

    override fun renderLinks() {
        super.renderLinks()
    }

    override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 {
        TODO("Not yet implemented")
    }

    override fun colouriser(): FrozenColorizer {
        TODO("Not yet implemented")
    }

    //endregion

    override fun loadModData(tag: CompoundTag?) {
        TODO("Not yet implemented")
    }

    override fun saveModData(tag: CompoundTag?) {
        TODO("Not yet implemented")
    }

    companion object {
        const val MAX_SQR_LINK_RANGE = 32.0*32.0
    }
}