package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import ram.talia.hexal.api.getItem

object OpCombineItems : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val absorber = args.getItem(0, argc)
        val absorbee = args.getItem(1, argc)

        if (absorber == null || absorbee == null)
            return listOf(absorber?.copy() ?: NullIota(), absorbee?.copy() ?: NullIota())
        if (absorber.itemIndex == absorbee.itemIndex)
            return listOf(absorber.copy())

        absorber.absorb(absorbee)

        return listOfNotNull(absorber.copy())
    }
}