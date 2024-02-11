@file:Suppress("CAST_NEVER_SUCCEEDS", "KotlinConstantConditions")

package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapEvalTooDeep
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.addBounded
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.common.entities.ProjectileWisp
import ram.talia.hexal.common.entities.TickingWisp
import java.lang.Integer.max

class OpSummonWisp(val ticking: Boolean) : SpellOperator {
    override val argc = if (ticking) 3 else 4

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val hex = args.getChecked<SpellList>(0, argc)
        val pos = args.getChecked<Vec3>(1, argc)
        val media: Double
        val cost: Int

        val mCast = ctx as? IMixinCastingContext
        if (mCast != null && mCast.hasWisp() && mCast.wisp.summonedChildThisCast) // wisps can only summon one child per cast.
            throw MishapEvalTooDeep()

        val spell = when (ticking) {
            true -> {
                media = args.getChecked(2, argc)
                cost = COST_SUMMON_WISP_TICKING.addBounded((media * ManaConstants.DUST_UNIT).toInt())
                Spell(true, pos, hex.toList(), (media * ManaConstants.DUST_UNIT).toInt())
            }
            false -> {
                val vel = args.getChecked<Vec3>(2, argc)
                media = args.getChecked(3, argc)
                cost = max((COST_SUMMON_WISP_PROJECTILE * vel.lengthSqr()).toInt(), COST_SUMMON_WISP_PROJECTILE_MIN)
                            .addBounded((media * ManaConstants.DUST_UNIT).toInt())
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
            // wisps can only summon one child per cast
            val mCast = ctx as? IMixinCastingContext
            if (mCast != null && mCast.hasWisp())
                mCast.wisp.summonedChildThisCast = true

            val colouriser = IXplatAbstractions.INSTANCE.getColorizer(ctx.caster)
            val wisp = when (ticking) {
                true -> TickingWisp(ctx.world, pos, ctx.caster, media)
                false -> ProjectileWisp(ctx.world, pos, vel, ctx.caster, media)
            }
            wisp.setColouriser(colouriser)
            wisp.serHex.set(hex)
            ctx.world.addFreshEntity(wisp)
        }
    }

    companion object {
        private const val COST_SUMMON_WISP_TICKING = 3 * ManaConstants.DUST_UNIT
        private const val COST_SUMMON_WISP_PROJECTILE = 3/1.75 * ManaConstants.DUST_UNIT
        private const val COST_SUMMON_WISP_PROJECTILE_MIN = ManaConstants.DUST_UNIT / 2
    }
}