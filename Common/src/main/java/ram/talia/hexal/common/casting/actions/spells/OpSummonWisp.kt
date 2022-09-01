package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.datafixers.util.Either
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.div
import ram.talia.hexal.common.entities.ProjectileWisp
import ram.talia.hexal.common.entities.TickingWisp

class OpSummonWisp(val ticking: Boolean) : SpellOperator {
    override val argc = if (ticking) 3 else 4

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val hex = args.getChecked<SpellList>(0, argc)
        val pos = args.getChecked<Vec3>(1, argc)
        val media: Double
        val cost: Int

        val spell = when (ticking) {
            true -> {
                media = args.getChecked(2, argc)
                cost = COST_SUMMON_WISP + (media * ManaConstants.DUST_UNIT).toInt()
                Spell(true, pos, hex.toList(), (media * ManaConstants.DUST_UNIT).toInt())
            }
            false -> {
                val vel = args.getChecked<Vec3>(2, argc)
                media = args.getChecked(3, argc)
                cost = (COST_SUMMON_WISP * vel.lengthSqr()).toInt() + (media * ManaConstants.DUST_UNIT).toInt()
                Spell(false, pos, hex.toList(), (media * ManaConstants.DUST_UNIT).toInt(), vel)
            }
        }

        ctx.assertVecInRange(pos)

        return Triple(
            spell,
            cost,
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val ticking: Boolean, val pos: Vec3, val hex: List<SpellDatum<*>>, val media: Int, val vel: Vec3 = Vec3.ZERO) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val colouriser = IXplatAbstractions.INSTANCE.getColorizer(ctx.caster)
            val wisp = when (ticking) {
                true -> TickingWisp(ctx.world, pos, ctx.caster, media, false)
                false -> ProjectileWisp(ctx.world, pos, vel, ctx.caster, media)
            }
            wisp.setColouriser(colouriser)
            wisp.hex = Either.left(hex)
            ctx.world.addFreshEntity(wisp)
        }
    }

    companion object {
        private const val COST_SUMMON_WISP = 3 * ManaConstants.DUST_UNIT
    }
}