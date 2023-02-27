package ram.talia.hexal.api.mediafieditems

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.math.max

object MediafiedItemManager {
    private val allStorages: MutableMap<UUID, WeakReference<BlockEntityMediafiedStorage>> = mutableMapOf() // TODO: clean this of nullified weak references at some point.
    @JvmStatic
    fun addStorage(id: UUID, storage: BlockEntityMediafiedStorage) = allStorages.set(id, WeakReference(storage))

    @JvmStatic
    fun removeStorage(id: UUID) = allStorages.remove(id)

    @JvmStatic
    fun getStorage(id: UUID) = allStorages[id]

    @JvmStatic
    fun isStorageFull(id: UUID) = allStorages[id]?.get()?.isFull()

    @JvmStatic
    fun getBoundStorage(player: ServerPlayer): UUID? = IXplatAbstractions.INSTANCE.getBoundStorage(player).takeIf { it in allStorages }

    @JvmStatic
    fun setBoundStorage(player: ServerPlayer, storage: UUID?) = IXplatAbstractions.INSTANCE.setBoundStorage(player, storage)

    @JvmStatic
    fun getAllRecords(storageId: UUID): Map<Index, ItemRecord>? {
        return allStorages[storageId]?.get()?.storedItems?.mapKeys { Index(storageId, it.key) }
    }

    @JvmStatic
    fun getAllContainedItemTypes(player: ServerPlayer): Set<Item>? {
        val storageId = getBoundStorage(player) ?: return null
        return getAllContainedItemTypes(storageId)
    }

    @JvmStatic
    fun getAllContainedItemTypes(storageId: UUID): Set<Item>? = allStorages[storageId]?.get()?.getAllContainedItemTypes()

    @JvmStatic
    fun getItemRecordsMatching(player: ServerPlayer, item: Item): Map<Index, ItemRecord>? {
        val storageId = getBoundStorage(player) ?: return null
        return getItemRecordsMatching(storageId, item)
    }

    @JvmStatic
    fun getItemRecordsMatching(storageId: UUID, item: Item): Map<Index, ItemRecord>? = allStorages[storageId]?.get()?.getItemRecordsMatching(item)?.mapKeys { Index(storageId, it.key) }

    @JvmStatic
    fun getItemRecordsMatching(player: ServerPlayer, itemRecord: ItemRecord): Map<Index, ItemRecord>? {
        val storageId = getBoundStorage(player) ?: return null
        return getItemRecordsMatching(storageId, itemRecord)
    }

    @JvmStatic
    fun getItemRecordsMatching(storageId: UUID, itemRecord: ItemRecord): Map<Index, ItemRecord>?
        = allStorages[storageId]?.get()?.getItemRecordsMatching(itemRecord)?.mapKeys { Index(storageId, it.key) }



    @JvmStatic
    fun assignItem(stack: ItemStack, uuid: UUID): Index? = allStorages[uuid]?.get()?.assignItem(ItemRecord(stack.copy())) // copied just in case
    @JvmStatic
    fun assignItem(record: ItemRecord, uuid: UUID): Index? = allStorages[uuid]?.get()?.assignItem(record)

    @JvmStatic
    fun contains(index: Index): Boolean
        = allStorages.contains(index.storage) && allStorages[index.storage]?.get()?.contains(index.index) ?: false

    private fun access(index: Index): ItemRecord? = allStorages[index.storage]?.get()?.storedItems?.get(index.index)

    @JvmStatic
    fun removeRecord(index: Index) {
        allStorages[index.storage]?.get()?.removeStoredItem(index.index)
    }

    @JvmStatic
    fun getRecord(index: Index): WeakReference<ItemRecord>? = access(index)?.let { WeakReference(it) }

    @JvmStatic
    fun getItem(index: Index): Item? = access(index)?.item

    @JvmStatic
    fun getTag(index: Index): CompoundTag? = access(index)?.tag

    @JvmStatic
    fun getCount(index: Index): Long? = access(index)?.count

    @JvmStatic
    fun getStacksToDrop(index: Index, count: Long): List<ItemStack>? {
        val drops = mutableListOf<ItemStack>()

        val record = access(index) ?: return null

        record.addDrops(count, drops)

        // if the stack is empty, remove it from the record.
        if (record.count <= 0)
            removeRecord(index)

        return drops
    }

    @JvmStatic
    fun merge(absorberIndex: Index, absorbeeIndex: Index) {
        val absorber = access(absorberIndex) ?: return
        val absorbee = access(absorbeeIndex) ?: return

        if (absorber.absorb(absorbee) && absorbee.count <= 0)
            removeRecord(absorbeeIndex)
    }

    @JvmStatic
    fun splitOff(splitterIndex: Index, amount: Long, storage: UUID?): Index? {
        if (amount <= 0)
            return null

        val splitter = access(splitterIndex) ?: return null

        val splittee = splitter.split(amount)

        if (splitter.count <= 0)
            removeRecord(splitterIndex)

        // if passed a non-null storage, try to use that. If not passed a storage, or if it didn't work, use the splitter's storage.
        return assignItem(splittee, storage ?: splitterIndex.storage) ?: assignItem(splittee, splitterIndex.storage)
    }

    @JvmStatic
    fun templateOff(index: Index, stack: ItemStack) {
        val record = access(index) ?: return
        record.item = stack.item
        record.tag = stack.tag?.copy()
    }

    @JvmStatic
    fun removeItems(index: Index, count: Long): Long {
        val record = access(index) ?: return 0
        val startingCount = record.count
        record.count = max(record.count - count, 0)
        val numRemoved = startingCount - record.count
        if (record.count <= 0)
            removeRecord(index)
        return numRemoved
    }


    data class Index(val storage: UUID, val index: Int) {
        fun writeToNbt(tag: CompoundTag) {
            tag.putUUID(TAG_STORAGE, storage)
            tag.putInt(TAG_INDEX, index)
        }

        companion object {
            const val TAG_STORAGE = "storage"
            const val TAG_INDEX = "index"

            @JvmStatic
            fun readFromNbt(tag: CompoundTag): Index {
                val storage = tag.getUUID(TAG_STORAGE)
                val index = tag.getInt(TAG_INDEX)
                return Index(storage, index)
            }
        }
    }
}