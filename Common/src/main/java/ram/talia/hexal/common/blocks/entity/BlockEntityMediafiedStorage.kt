package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.api.mediafieditems.ItemRecord
import ram.talia.hexal.common.lib.HexalBlockEntities
import java.util.UUID

class BlockEntityMediafiedStorage(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.MEDIAFIED_STORAGE, pos, state) {

    val uuid: UUID get() = id
    private var id = UUID.randomUUID()


    val storedItems: MutableMap<Int, ItemRecord> = mutableMapOf()

    fun contains(index: Int) = storedItems.contains(index)

    override fun saveModData(tag: CompoundTag) {
        tag.putUUID(TAG_UUID, uuid)
    }

    override fun loadModData(tag: CompoundTag) {
        if (tag.contains(TAG_UUID))
            id = tag.getUUID(TAG_UUID)
    }

    companion object {
        const val TAG_UUID = "hexal:uuid"
    }
}