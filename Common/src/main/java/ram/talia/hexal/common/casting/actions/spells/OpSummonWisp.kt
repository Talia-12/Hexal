package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.common.entities.BaseWisp

object OpSummonWisp : SpellOperator {
    private const val COST_SUMMON_WISP = ManaConstants.CRYSTAL_UNIT

    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val pos = args.getChecked<Vec3>(0, argc)
        val vel = args.getChecked<Vec3>(1, argc)

        ctx.assertVecInRange(pos)

        return Triple(
            Spell(pos, vel),
            COST_SUMMON_WISP,
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val pos: Vec3, val vel: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val wisp = BaseWisp(ctx.world, pos, ctx.caster)
            wisp.shoot(vel.x, vel.y, vel.z, vel.length().toFloat(), 0.0f)
            ctx.world.addFreshEntity(wisp)
        }
    }
}