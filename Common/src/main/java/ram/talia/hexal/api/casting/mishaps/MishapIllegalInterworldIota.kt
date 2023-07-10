package ram.talia.hexal.api.casting.mishaps

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import ram.talia.hexal.api.casting.iota.GateIota
import ram.talia.hexal.api.casting.iota.MoteIota

class MishapIllegalInterworldIota(val iota: Iota) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.GREEN)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = error("illegal_interworld_iota", iota.display())

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.caster?.let { it.health /= 2 } // Bad but better than freaking TODO()
    }

    companion object {
        fun getFromNestedIota(iota: Iota): Iota? {
            val poolToSearch = ArrayDeque<Iota>()
            poolToSearch.addLast(iota)

            while (poolToSearch.isNotEmpty()) {
                val iotaToCheck = poolToSearch.removeFirst()
                if (iotaToCheck is GateIota || iotaToCheck is MoteIota)
                    return iotaToCheck
                if (iotaToCheck is ListIota)
                    poolToSearch.addAll(iotaToCheck.list)
            }

            return null
        }

        fun replaceInNestedIota(iota: Iota): Iota {
            return when (iota) {
                is GateIota -> GarbageIota()
                is MoteIota -> GarbageIota()
                is ListIota -> iota.list.map { replaceInNestedIota(it) }.asActionResult[0]
                else -> iota
            }
        }
    }
}