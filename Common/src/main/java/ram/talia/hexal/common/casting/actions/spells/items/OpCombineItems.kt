package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.getItem

object OpCombineItems : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val absorber = args.getItem(0, argc)
        val absorbee = args.getItem(0, argc)

        if (absorber == null || absorbee == null)
            return listOfNotNull(absorber, absorbee)

        absorber.absorb(absorbee)

        // in the rare rare case where the contents of the absorbee didn't completely
        // fit into the absorber, return the absorbee back to the stack.
        return listOfNotNull(absorber, absorbee.selfOrNull())
    }
}