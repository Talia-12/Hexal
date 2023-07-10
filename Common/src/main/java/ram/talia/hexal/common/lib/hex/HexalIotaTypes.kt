package ram.talia.hexal.common.lib.hex

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.casting.iota.GateIota
import ram.talia.hexal.api.casting.iota.MoteIota
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
    val GATE: IotaType<GateIota> = type("gate", GateIota.TYPE)
    @JvmField
    val ITEM: IotaType<MoteIota> = type("item", MoteIota.TYPE)

    private fun <U : Iota, T : IotaType<U>> type(name: String, type: T): T {
        val old = TYPES.put(modLoc(name), type)
        require(old == null) { "Typo? Duplicate id $name" }
        return type
    }
}