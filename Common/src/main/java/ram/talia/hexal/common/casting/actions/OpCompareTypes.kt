package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota

object OpCompareTypes : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        // simple, but results in e.g. Horse and Cow being 0 since they're different classes
        // could change to pool all animals together, all monsters together, etc.
        return (args[0].type == args[1].type).asActionResult
    }
}