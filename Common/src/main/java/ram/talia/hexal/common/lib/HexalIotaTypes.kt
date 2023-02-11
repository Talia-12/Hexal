package ram.talia.hexal.common.lib

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.IotaType
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.spell.iota.EntityTypeIota
import ram.talia.hexal.api.spell.iota.GateIota
import ram.talia.hexal.api.spell.iota.IotaTypeIota
import ram.talia.hexal.api.spell.iota.ItemTypeIota
import java.util.function.BiConsumer

object HexalIotaTypes {
    @JvmStatic
    @ApiStatus.Internal
    fun registerTypes() {
        val r = BiConsumer { type: IotaType<*>, id: ResourceLocation -> Registry.register(HexIotaTypes.REGISTRY, id, type) }
        for ((key, value) in TYPES) {
            r.accept(value, key)
        }
    }

    private val TYPES: MutableMap<ResourceLocation, IotaType<*>> = LinkedHashMap()

    @JvmField
    val IOTA_TYPE: IotaType<IotaTypeIota> = type("iota_type", IotaTypeIota.TYPE)
    @JvmField
    val ENTITY_TYPE: IotaType<EntityTypeIota> = type("entity_type", EntityTypeIota.TYPE)
    @JvmField
    val ITEM_TYPE: IotaType<ItemTypeIota> = type("item_type", ItemTypeIota.TYPE)
    @JvmField
    val GATE_TYPE: IotaType<GateIota> = type("gate", GateIota.TYPE)

    private fun <U : Iota, T : IotaType<U>> type(name: String, type: T): T {
        val old = TYPES.put(modLoc(name), type)
        require(old == null) { "Typo? Duplicate id $name" }
        return type
    }
}