package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import ram.talia.hexal.api.getItem
import ram.talia.hexal.api.getStrictlyPositiveInt

object OpSplitItem : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val item = args.getItem(0, argc) ?: return listOf(NullIota())
        val toSplitOff = args.getStrictlyPositiveInt(0, argc)

        val split = item.splitOff(toSplitOff) ?: return listOf(item.copy(), NullIota())
        return listOf(item.copy(), split.copy())
    }
}