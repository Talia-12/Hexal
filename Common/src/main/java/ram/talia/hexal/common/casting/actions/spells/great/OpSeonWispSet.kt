package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getBaseCastingWisp
import ram.talia.hexal.api.casting.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.xplat.IXplatAbstractions

object OpSeonWispSet : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val wisp = args.getBaseCastingWisp(0, argc)

        if (wisp.caster != env.caster)
            throw MishapOthersWisp(wisp.caster)

        return SpellAction.Result(Spell(wisp), HexalConfig.server.seonWispSetCost, listOf(ParticleSpray.burst(wisp.position(), 1.0)))
    }

    private data class Spell(val wisp: BaseCastingWisp) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // seon can only be changed once the previous seon has died.
            val lastSeon = IXplatAbstractions.INSTANCE.getSeon(env.caster)
            if (lastSeon == null || lastSeon.isRemoved)
                IXplatAbstractions.INSTANCE.setSeon(env.caster, wisp)
        }
    }
}