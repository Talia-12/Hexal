package ram.talia.hexal.common.lib

import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.xplat.IXplatAbstractions
import java.util.function.BiConsumer

object HexalItems {
    @JvmStatic
    fun registerItems(r: BiConsumer<Item, ResourceLocation>) {
        for ((key, value) in ITEMS) {
            r.accept(value, key)
        }
    }

    private val ITEMS: MutableMap<ResourceLocation, Item> = LinkedHashMap()

    @JvmField
    val RELAY = item("relay", IXplatAbstractions.INSTANCE.getItemRelay(HexItems.props()))

    private fun <T : Item> item(name: String, item: T): T {
        val old = ITEMS.put(HexalAPI.modLoc(name), item)
        require(old == null) { "Typo? Duplicate id $name" }
        return item
    }
}