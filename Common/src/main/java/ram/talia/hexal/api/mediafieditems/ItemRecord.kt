package ram.talia.hexal.api.mediafieditems

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

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
