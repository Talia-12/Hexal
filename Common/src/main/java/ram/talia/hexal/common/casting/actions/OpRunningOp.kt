package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.reductions

class OpRunningOp(private val initial: (Iota?) -> Iota, private val operator: (Iota, Iota) -> Iota) : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        try {
            val list = args.getList(0, argc)
            return list.reductions(initial(list.toList().getOrNull(0)), operator).toList().asActionResult
        } catch (e: InvalidIotaException) {
            throw MishapInvalidIota.ofType(args[0], 0, e.expectedType)
        }
    }

    data class InvalidIotaException(val expectedType: String) : Exception()
}