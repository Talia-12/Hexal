package ram.talia.hexal.api.mediafieditems

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions
import java.lang.ref.WeakReference
import java.util.UUID

object MediafiedItemManager {
    private val allItems: MutableMap<UUID, WeakReference<BlockEntityMediafiedStorage>> = mutableMapOf() // TODO: clean this of nullified weak references at some point.
    @JvmStatic
    fun addStorage(id: UUID, storage: BlockEntityMediafiedStorage) = allItems.set(id, WeakReference(storage))

    @JvmStatic
    fun removeStorage(id: UUID) = allItems.remove(id)

    @JvmStatic
    fun getBoundStorage(player: ServerPlayer): UUID? = IXplatAbstractions.INSTANCE.getBoundStorage(player)

    @JvmStatic
    fun setBoundStorage(player: ServerPlayer, storage: UUID?) = IXplatAbstractions.INSTANCE.setBoundStorage(player, storage)

    @JvmStatic
    fun assignItem(stack: ItemStack, uuid: UUID): Index? = allItems[uuid]?.get()?.assignItem(ItemRecord(stack.copy())) // copied just in case
    @JvmStatic
    fun assignItem(record: ItemRecord, uuid: UUID): Index? = allItems[uuid]?.get()?.assignItem(record)

    @JvmStatic
    fun contains(index: Index): Boolean
        = allItems.contains(index.storage) && allItems[index.storage]?.get()?.contains(index.index) ?: false

    private fun access(index: Index): ItemRecord? = allItems[index.storage]?.get()?.storedItems?.get(index.index)

    private fun remove(index: Index) {
        allItems[index.storage]?.get()?.storedItems?.remove(index.index)
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
            remove(index)

        return drops
    }

    @JvmStatic
    fun merge(absorberIndex: Index, absorbeeIndex: Index) {
        val absorber = access(absorberIndex) ?: return
        val absorbee = access(absorbeeIndex) ?: return

        if (absorber.absorb(absorbee) && absorbee.count <= 0)
            remove(absorbeeIndex)
    }

    @JvmStatic
    fun splitOff(splitterIndex: Index, amount: Long): Index? {
        if (amount <= 0)
            return null

        val splitter = access(splitterIndex) ?: return null

        val splittee = splitter.split(amount)

        if (splitter.count <= 0)
            remove(splitterIndex)

        return assignItem(splittee, splitterIndex.storage)
    }

    @JvmStatic
    fun templateOff(index: Index, stack: ItemStack) {
        val record = access(index) ?: return
        record.item = stack.item
        record.tag = stack.tag?.copy()
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