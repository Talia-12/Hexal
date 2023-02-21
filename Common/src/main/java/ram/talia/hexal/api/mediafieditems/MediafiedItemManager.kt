package ram.talia.hexal.api.mediafieditems

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.lang.ref.WeakReference

object MediafiedItemManager {
    private var currentItemNum = 0

    private val allItems: MutableMap<Int, ItemRecord> = mutableMapOf()

    @JvmStatic
    fun contains(index: Int): Boolean = allItems.contains(index)

    @JvmStatic
    fun getRecord(index: Int): WeakReference<ItemRecord>? = allItems[index]?.let { WeakReference(it) }

    @JvmStatic
    fun getItem(index: Int): Item? = allItems[index]?.item

    @JvmStatic
    fun getTag(index: Int): CompoundTag? = allItems[index]?.tag

    @JvmStatic
    fun getCount(index: Int): Int? = allItems[index]?.count

    @JvmStatic
    fun getDisplayName(index: Int): Component? = allItems[index]?.getDisplayName()

    data class ItemRecord(var item: Item, var tag: CompoundTag?, var count: Int) {
        fun typeMatches(other: ItemRecord): Boolean {
            return item == other.item && tag == other.tag
        }

        /**
         * Absorb the contents of another [ItemRecord] that matches this one,
         * increasing this record's count by the other's. Doesn't set the other's
         * count to 0 or remove it.
         */
        fun absorb(other: ItemRecord) {
            if (!typeMatches(other))
                return

            count += other.count
        }

        fun getDisplayName(): Component {
            val itemStack = ItemStack(item)
            itemStack.tag = tag // don't need to copy tag since stack isn't used for anything.
            return itemStack.hoverName
        }
    }
}