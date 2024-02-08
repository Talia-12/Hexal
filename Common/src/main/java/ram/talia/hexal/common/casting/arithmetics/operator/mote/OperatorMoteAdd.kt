package ram.talia.hexal.common.casting.arithmetics.operator.mote

import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBasic
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate.ofType
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.common.casting.arithmetics.operator.nextMote
import ram.talia.hexal.common.lib.hex.HexalIotaTypes.MOTE

object OperatorMoteAdd : OperatorBasic(2, IotaMultiPredicate.all(ofType(MOTE))) {
    override fun apply(iotas: Iterable<Iota>, env: CastingEnvironment): Iterable<Iota> {
        val it = iotas.iterator().withIndex()

        val absorber = it.nextMote(arity)
        val absorbee = it.nextMote(arity)

        if (absorber == null || absorbee == null) {
            // ensure always 1 iota returned to the stack.
            val toReturn = listOfNotNull(absorber?.copy(), absorbee?.copy())
            return toReturn.ifEmpty { null.asActionResult }
        }
        if (absorber.itemIndex == absorbee.itemIndex)
            return listOf(absorber.copy())

        if (!absorber.typeMatches(absorbee))
            throw MishapInvalidIota.of(absorbee, 0, "cant_combine_motes")

        absorber.absorb(absorbee)

        return listOfNotNull(absorber.copy())
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        TODO("Not yet implemented")
    }
}