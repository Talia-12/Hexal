package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.putList
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.api.mediafieditems.ItemRecord
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.common.lib.HexalBlockEntities
import java.util.UUID

class BlockEntityMediafiedStorage(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.MEDIAFIED_STORAGE, pos, state) {

    val uuid: UUID get() = id
    private var id = UUID.randomUUID()

    private var currentItemIndex = 0

    val storedItems: MutableMap<Int, ItemRecord> = mutableMapOf()

    fun contains(index: Int) = storedItems.contains(index)

    fun assignItem(itemRecord: ItemRecord): MediafiedItemManager.Index {
        val index = currentItemIndex
        storedItems[index] = itemRecord
        currentItemIndex += 1
        return MediafiedItemManager.Index(uuid, index)
    }

    override fun saveModData(tag: CompoundTag) {
        tag.putUUID(TAG_UUID, uuid)
        tag.putInt(TAG_INDEX, currentItemIndex)

        val stored = ListTag()

        for ((id, record) in storedItems) {
            val c = CompoundTag()
            c.putInt(TAG_ID, id)
            record.writeToTag(c)
            stored.add(c)
        }

        tag.putList(TAG_STORED, stored)
    }

    override fun loadModData(tag: CompoundTag) {
        if (tag.contains(TAG_UUID))
            id = tag.getUUID(TAG_UUID)
        if (tag.contains(TAG_INDEX))
            currentItemIndex = tag.getInt(TAG_INDEX)

        if (tag.contains(TAG_STORED)) {
            val stored = tag.getList(TAG_STORED, Tag.TAG_COMPOUND)

            for (entry in stored) {
                val cEntry = entry as CompoundTag
                val record = ItemRecord.readFromTag(cEntry)
                if (record != null)
                    storedItems[cEntry.getInt(TAG_ID)] = record
            }
        }
    }

    companion object {
        const val TAG_UUID = "hexal:uuid"
        const val TAG_INDEX = "hexal:index"
        const val TAG_STORED = "hexal:stored"

        const val TAG_ID = "hexal:storage_id"
    }
}