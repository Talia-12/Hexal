package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.getBaseWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

object OpWispOwner : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val wisp = args.getBaseWisp(0, argc)
        val owner = args.getEntity(1, argc)

        return (wisp is BaseCastingWisp && wisp.caster == owner).asActionResult
    }
}