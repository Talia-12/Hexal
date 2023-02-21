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

    /**
     * Much of the structure of this class comes from AE2's [AEItemKey](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/9ff272a869508125daf5727746c9d9b8b00248bd/src/main/java/appeng/api/stacks/AEItemKey.java).
     */
    data class ItemRecord(var item: Item, var tag: CompoundTag?, var count: Long) {
        constructor(stack: ItemStack) : this(stack.item, stack.tag, stack.count.toLong())

        fun typeMatches(other: ItemRecord): Boolean {
            return item == other.item && tag == other.tag
        }

        /**
         * Absorb the contents of another [ItemRecord] that matches this one,
         * increasing this record's count by the other's. Doesn't set the other's
         * count to 0 or remove it.
         */
        fun absorb(other: ItemRecord): Boolean {
            if (!typeMatches(other))
                return false

            // protection against overflow errors (really shouldn't happen but ya know why not
            val oldCount = count
            count = count.addBounded(other.count)
            other.count -= (count - oldCount)

            return true
        }

        fun getDisplayName(): Component {
            val itemStack = ItemStack(item)
            itemStack.tag = tag // don't need to copy tag since stack isn't used for anything.
            return itemStack.hoverName
        }

        fun toStack(): ItemStack = toStack(1)

        fun toStack(count: Int): ItemStack {
            if (count <= 0) {
                return ItemStack.EMPTY
            }

            val result = ItemStack(item)
            result.tag = tag?.copy()
            result.count = count
            return result
        }

        fun addDrops(amount: Long, drops: MutableList<ItemStack>) {
            var leftToTake = min(amount, count)

            while (leftToTake > 0) {
                if (drops.size > 1000) {
                    HexalAPI.LOGGER.warn("Tried dropping an excessive amount of items, ignoring $leftToTake $item")
                    break
                }
                val taken = min(leftToTake, item.maxStackSize.toLong())
                leftToTake -= taken
                drops.add(toStack(taken.toInt()))
            }

            // subtracts the amount taken from the remaining.
            // unless it attempted to make more than 1000 stacks,
            // this should simplify to count - amount
            // the max(..., 0) is to make sure that if the original
            // amount was greater than count it doesn't result in
            // count being less than 0.
            count = max(count - amount + leftToTake, 0)
        }

        companion object {
            /**
             * Taken from https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/9ff272a869508125daf5727746c9d9b8b00248bd/src/main/java/appeng/api/stacks/AEItemKey.java#L130
             */
            fun fromTag(tag: CompoundTag): ItemRecord? {
                return null
//                return try {
//                    val item = Registries.ITEM.getOptional(ResourceLocation(tag.getString("id"))).orElseThrow { IllegalArgumentException("Unknown item id.") }
//                    val extraTag = if (tag.contains("tag")) tag.getCompound("tag") else null
//                    val extraCaps = if (tag.contains("caps")) tag.getCompound("caps") else null
//                    of(item, extraTag, extraCaps)
//                } catch (e: Exception) {
//                    AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e)
//                    null
//                }
            }
        }
    }
}