package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.common.entities.ProjectileLemma
import ram.talia.hexal.common.entities.TickingLemma

class OpSummonLemma(val ticking: Boolean) : SpellOperator {
    override val argc = if (ticking) 3 else 4

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val hex = args.getChecked<SpellList>(0, argc)
        val pos = args.getChecked<Vec3>(1, argc)
        val media: Double

        val spell = when (ticking) {
            true -> {
                media = args.getChecked(2, argc)
                Spell(true, pos, hex.toList(), (media * ManaConstants.DUST_UNIT).toInt())
            }
            false -> {
                val vel = args.getChecked<Vec3>(2, argc)
                media = args.getChecked(3, argc)
                Spell(false, pos, hex.toList(), (media * ManaConstants.DUST_UNIT).toInt(), vel)
            }
        }

        ctx.assertVecInRange(pos)

        return Triple(
            spell,
            COST_SUMMON_LEMMA + (media * ManaConstants.DUST_UNIT).toInt(),
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val ticking: Boolean, val pos: Vec3, val hex: List<SpellDatum<*>>, val media: Int, val vel: Vec3 = Vec3.ZERO) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val colouriser = IXplatAbstractions.INSTANCE.getColorizer(ctx.caster)
            val lemma = when (ticking) {
                true -> TickingLemma(ctx.world, pos, ctx.caster, media, false)
                false -> ProjectileLemma(ctx.world, pos, vel, ctx.caster, media)
            }
            lemma.setColouriser(colouriser)
            lemma.hex = hex
            ctx.world.addFreshEntity(lemma)
        }
    }

    companion object {
        private const val COST_SUMMON_LEMMA = ManaConstants.CRYSTAL_UNIT
    }
}