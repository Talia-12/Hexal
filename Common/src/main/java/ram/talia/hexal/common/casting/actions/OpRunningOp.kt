package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import ram.talia.hexal.api.reductions

class OpRunningOp(private val initial: Double, private val operator: (Double, SpellDatum<*>) -> Double) : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        try {
            return args.getChecked<SpellList>(0, argc)
                    .reductions(initial, operator).map { SpellDatum.make(it) }.toList().asSpellResult
        } catch (e: InvalidIotaException) {
            throw MishapInvalidIota(args[0], 0,
                    "hexcasting.mishap.invalid_value.class.${e.expectedType}".asTranslatedComponent)
        }
    }

    data class InvalidIotaException(val expectedType: String) : Exception()
}