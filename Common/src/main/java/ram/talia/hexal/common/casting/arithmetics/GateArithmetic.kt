package ram.talia.hexal.common.casting.arithmetics

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.ABS
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.APPEND
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.all
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate.ofType
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import ram.talia.hexal.api.casting.iota.GateIota
import ram.talia.hexal.common.lib.hex.HexalIotaTypes.GATE
import java.util.function.Function

object GateArithmetic : Arithmetic {
    private val OPS = listOf(
        ABS
    )

    override fun arithName(): String = "gate_ops"

    override fun opTypes(): Iterable<HexPattern> = OPS

    override fun getOperator(pattern: HexPattern): Operator = when (pattern) {
        ABS -> make1Double { it.numMarked.toDouble() }
        else -> throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.")
    }

    private fun make1Double(op: Function<GateIota, Double>): OperatorUnary = OperatorUnary(all(ofType(GATE)))
    { i: Iota -> DoubleIota(
            op.apply(Operator.downcast(i, GATE))
    ) }
}