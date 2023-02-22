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
    fun contains(index: Index): Boolean
        = allItems.contains(index.storage) && allItems[index.storage]?.get()?.contains(index.index) ?: false

    @JvmStatic
    fun getRecord(index: Index): WeakReference<ItemRecord>? = allItems[index]?.let { WeakReference(it) }

    @JvmStatic
    fun getItem(index: Index): Item? = allItems[index]?.item

    @JvmStatic
    fun getTag(index: Index): CompoundTag? = allItems[index]?.tag

    @JvmStatic
    fun getCount(index: Index): Int? = allItems[index]?.count

    @JvmStatic
    fun getDisplayName(index: Index): Component? = allItems[index]?.getDisplayName()

    data class Index(val storage: UUID, val index: Int)
}