package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.getBaseWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

object OpWispOwner : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val wisp = args.getBaseWisp(0, argc)
        val owner = args.getEntity(1, argc)

        return (wisp is BaseCastingWisp && wisp.caster == owner).asActionResult
    }
}