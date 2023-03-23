package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.getItem

object OpItemsCombinable : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val typer = args.getItem(0, OpCombineItems.argc) ?: return false.asActionResult
        val typee = args.getItem(1, OpCombineItems.argc) ?: return false.asActionResult

        return (typer.itemIndex != typee.itemIndex && typer.typeMatches(typee)).asActionResult
    }
}