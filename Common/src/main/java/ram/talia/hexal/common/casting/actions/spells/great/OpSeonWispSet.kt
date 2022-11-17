package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.spell.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.xplat.IXplatAbstractions

object OpSeonWispSet : SpellOperator {
    const val MAKE_SEON_COST = 5 * ManaConstants.CRYSTAL_UNIT

    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val wisp = args.getChecked<BaseCastingWisp>(0, argc)

        if (wisp.caster != ctx.caster)
            throw MishapOthersWisp(wisp.caster)

        return Triple(Spell(wisp), MAKE_SEON_COST, listOf(ParticleSpray.burst(wisp.position(), 1.0)))
    }

    private data class Spell(val wisp: BaseCastingWisp) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            IXplatAbstractions.INSTANCE.setSeon(ctx.caster, wisp)
        }
    }
}