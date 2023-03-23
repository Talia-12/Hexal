package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.getItem

object OpCombineItems : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val absorber = args.getItem(0, argc)
        val absorbee = args.getItem(1, argc)

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