package ram.talia.hexal.common.casting.arithmetics

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.ABS
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.ADD
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.all
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate.ofType
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.common.casting.arithmetics.operator.mote.OperatorMoteAdd
import ram.talia.hexal.common.casting.arithmetics.operator.mote.OperatorMoteExtractItem
import ram.talia.hexal.common.lib.hex.HexalIotaTypes.MOTE
import ram.talia.moreiotas.common.casting.arithmetic.ItemArithmetic.EXTRACT_ITEM
import java.util.function.Function

object MoteArithmetic : Arithmetic {
    private val OPS = listOf(
        ADD,
        ABS,
        EXTRACT_ITEM
    )

    override fun arithName() = "mote_ops"

    override fun opTypes(): Iterable<HexPattern> = OPS

    override fun getOperator(pattern: HexPattern): Operator = when (pattern) {
        ADD -> OperatorMoteAdd
        ABS -> make1Double { it.count.toDouble() }
        EXTRACT_ITEM -> OperatorMoteExtractItem
        else -> throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.")
    }

    private fun make1Double(op: Function<MoteIota, Double>): OperatorUnary = OperatorUnary(all(ofType(MOTE)))
    { i: Iota -> DoubleIota(
        op.apply(Operator.downcast(i, MOTE))
    ) }
}