package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.BaseWisp
import ram.talia.hexal.common.entities.ProjectileWisp

object OpSummonWisp : SpellOperator {
    private const val COST_SUMMON_WISP = ManaConstants.CRYSTAL_UNIT

    override val argc = 4

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val hex = args.getChecked<SpellList>(0, argc)
        val pos = args.getChecked<Vec3>(1, argc)
        val vel = args.getChecked<Vec3>(2, argc)
        val media = args.getChecked<Double>(3, argc)

        ctx.assertVecInRange(pos)

        return Triple(
            Spell(pos, vel, hex.toList(), (media * ManaConstants.DUST_UNIT).toInt()),
            COST_SUMMON_WISP + (media * ManaConstants.DUST_UNIT).toInt(),
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val pos: Vec3, val vel: Vec3, val hex: List<SpellDatum<*>>, val media: Int) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val colouriser = IXplatAbstractions.INSTANCE.getColorizer(ctx.caster)
            val wisp = ProjectileWisp(ctx.world, pos, ctx.caster, media)
            wisp.setColouriser(colouriser)
            wisp.shoot(vel.x, vel.y, vel.z, vel.length().toFloat(), 0.0f)
            wisp.onCollisionHex = hex
            ctx.world.addFreshEntity(wisp)
        }
    }
}