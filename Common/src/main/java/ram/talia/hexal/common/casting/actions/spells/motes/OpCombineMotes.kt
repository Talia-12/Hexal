package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.api.getMote

object OpCombineMotes : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val absorber = args.getMote(0, argc)
        val absorbee = args.getMote(1, argc)

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
}