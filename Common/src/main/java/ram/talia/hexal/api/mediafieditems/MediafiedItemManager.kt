package ram.talia.hexal.api.mediafieditems

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import java.lang.ref.WeakReference
import java.util.UUID

object MediafiedItemManager {
    private var currentItemNum = 0

    private val allItems: MutableMap<UUID, WeakReference<BlockEntityMediafiedStorage>> = mutableMapOf()

    @JvmStatic
    fun assignItem(stack: ItemStack, uuid: UUID): Int {
        val num = currentItemNum
        allItems[num] = ItemRecord(stack.copy()) // copied just in case
        currentItemNum += 1
        return num
    }

    @JvmStatic
    fun contains(index: Index): Boolean
        = allItems.contains(index.storage) && allItems[index.storage]?.get()?.contains(index.index) ?: false

    @JvmStatic
    fun getRecord(index: Index): WeakReference<ItemRecord>? = allItems[index]?.let { WeakReference(it) }

    @JvmStatic
    fun getItem(index: Index): Item? = allItems[index]?.item

    @JvmStatic
    fun getTag(index: Index): CompoundTag? = allItems[index]?.tag

    @JvmStatic
    fun getCount(index: Index): Long? = allItems[index]?.count

    @JvmStatic
    fun getDisplayName(index: Index): Component? = allItems[index]?.getDisplayName()

    @JvmStatic
    fun getStacksToDrop(index: Index, count: Long): List<ItemStack>? {
        val drops = mutableListOf<ItemStack>()

        val record = allItems[index] ?: return null

        record.addDrops(count, drops)

        // if the stack is empty, remove it from the record.
        if (record.count <= 0)
            allItems.remove(index)

        return drops
    }

    @JvmStatic
    fun merge(absorberIndex: Int, absorbeeIndex: Int) {
        val absorber = allItems[absorberIndex] ?: return
        val absorbee = allItems[absorbeeIndex] ?: return

        if (absorber.absorb(absorbee) && absorbee.count <= 0)
            allItems.remove(absorbeeIndex)
    }

    @JvmStatic
    fun splitOff(splitterIndex: Index, amount: Long): Int? {
        if (amount <= 0)
            return null

        val splitter = allItems[splitterIndex] ?: return null

        val splittee = splitter.split(amount)

        if (splitter.count <= 0)
            allItems.remove(splitterIndex)

        val num = currentItemNum
        allItems[num] = splittee
        currentItemNum += 1
        return num
    }

    @JvmStatic
    fun templateOff(index: Index, stack: ItemStack) {
        val record = allItems[index] ?: return
        record.item = stack.item
        record.tag = stack.tag?.copy()
    }


    data class Index(val storage: UUID, val index: Int)
}