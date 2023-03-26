package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.GarbageIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import ram.talia.hexal.api.spell.iota.GateIota
import ram.talia.hexal.api.spell.iota.ItemIota

class MishapIllegalInterworldIota(val iota: Iota) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.GREEN)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("illegal_interworld_iota", iota.display())

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        TODO("Not yet implemented")
    }

    companion object {
        fun getFromNestedIota(iota: Iota): Iota? {
            val poolToSearch = ArrayDeque<Iota>()
            poolToSearch.addLast(iota)

            while (poolToSearch.isNotEmpty()) {
                val iotaToCheck = poolToSearch.removeFirst()
                if (iotaToCheck is GateIota || iotaToCheck is ItemIota)
                    return iotaToCheck
                if (iotaToCheck is ListIota)
                    poolToSearch.addAll(iotaToCheck.list)
            }

            return null
        }

        fun replaceInNestedIota(iota: Iota): Iota {
            return when (iota) {
                is GateIota -> GarbageIota()
                is ItemIota -> GarbageIota()
                is ListIota -> iota.list.map { replaceInNestedIota(it) }.asActionResult[0]
                else -> iota
            }
        }
    }
}