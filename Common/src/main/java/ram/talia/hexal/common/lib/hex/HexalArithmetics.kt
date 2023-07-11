package ram.talia.hexal.common.lib.hex

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.common.casting.arithmetics.GateArithmetic
import ram.talia.hexal.common.casting.arithmetics.MoteArithmetic
import java.util.function.BiConsumer

object HexalArithmetics {
    @JvmStatic
    fun register(r: BiConsumer<Arithmetic, ResourceLocation>) {
        for ((key, value) in ARITHMETICS) {
            r.accept(value, key)
        }
    }

    private val ARITHMETICS: MutableMap<ResourceLocation, Arithmetic> = LinkedHashMap()

    val MOTE: MoteArithmetic = make("mote", MoteArithmetic)
    val GATE: GateArithmetic = make("gate", GateArithmetic)

    private fun <T : Arithmetic> make(name: String, arithmetic: T): T {
        val old = ARITHMETICS.put(modLoc(name), arithmetic)
        require(old == null) { "Typo? Duplicate id $name" }
        return arithmetic
    }
}