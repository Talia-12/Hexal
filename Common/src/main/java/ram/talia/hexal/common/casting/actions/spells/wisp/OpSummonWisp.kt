@file:Suppress("CAST_NEVER_SUCCEEDS", "KotlinConstantConditions")

package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.addBounded
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapExcessiveReproduction
import ram.talia.hexal.common.entities.ProjectileWisp
import ram.talia.hexal.common.entities.TickingWisp
import java.lang.Integer.max

class OpSummonWisp(val ticking: Boolean) : SpellAction {
    override val argc = if (ticking) 3 else 4

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val hex = args.getList(0, argc)
        val pos = args.getVec3(1, argc)
        val media: Double
        val cost: Int

        val mCast = env as? IMixinCastingContext
        if (mCast != null && mCast.hasWisp() && mCast.wisp!!.summonedChildThisCast)
            throw MishapExcessiveReproduction(mCast.wisp!!) // wisps can only summon one child per cast.

        val spell = when (ticking) {
            true -> {
                media = args.getPositiveDouble(2, argc)
                cost = HexalConfig.server.summonTickingWispCost.addBounded ((media * MediaConstants.DUST_UNIT).toInt())
                Spell(true, pos, hex.toList(), (media * MediaConstants.DUST_UNIT).toInt())
            }
            false -> {
                val vel = args.getVec3(2, argc)
                media = args.getPositiveDouble(3, argc)
                cost = max((HexalConfig.server.summonProjectileWispCost * vel.lengthSqr()).toInt(), HexalConfig.server.summonProjectileWispMinCost)
                            .addBounded((media * MediaConstants.DUST_UNIT).toInt())
                Spell(false, pos, hex.toList(), (media * MediaConstants.DUST_UNIT).toInt(), vel)
            }
        }

        env.assertVecInRange(pos)

        return SpellAction.Result(
            spell,
            cost,
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val ticking: Boolean, val pos: Vec3, val hex: List<Iota>, val media: Int, val vel: Vec3 = Vec3.ZERO) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // wisps can only summon one child per cast
            val mCast = env as? IMixinCastingContext
            if (mCast != null && mCast.hasWisp())
                mCast.wisp!!.summonedChildThisCast = true

            val pigment = IXplatAbstractions.INSTANCE.getPigment(env.caster)
            val wisp = when (ticking) {
                true -> TickingWisp(env.world, pos, env.caster, media)
                false -> ProjectileWisp(env.world, pos, vel, env.caster, media)
            }
            wisp.setColouriser(pigment)
            wisp.setHex(hex.toMutableList())
            env.world.addFreshEntity(wisp)
        }
    }
}