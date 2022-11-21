package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.reductions

class OpRunningOp(private val operator: (Double, Iota) -> Double) : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        try {
            return args.getList(0, argc).reductions(0.0, operator).map { DoubleIota(it) }.toList().asActionResult
        } catch (e: InvalidIotaException) {
            throw MishapInvalidIota.ofType(args[0], 0, e.expectedType)
        }
    }

    data class InvalidIotaException(val expectedType: String) : Exception()
}