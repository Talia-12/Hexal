package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import com.mojang.datafixers.util.Either
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.*
import ram.talia.hexal.common.network.MsgParticleLinesAck
import ram.talia.hexal.common.network.MsgSingleParticleAck
import ram.talia.hexal.xplat.IXplatAbstractions

object OpParticles : SpellAction {
    const val COST_PER_VEC = MediaConstants.DUST_UNIT / 10

    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val loc = args.getVec3OrListVec3(0, argc)

        // assert all locs in ambit.
        loc.map({ ctx.assertVecInRange(it) }, { ctx.assertVecListInRange(it, 32.0) })

        return Triple(
                Spell(loc),
                loc.map({ COST_PER_VEC }, { it.size * COST_PER_VEC }),
                listOf()
        )
    }

    data class Spell(val loc: Either<Vec3, List<Vec3>>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val colouriser = at.petrak.hexcasting.xplat.IXplatAbstractions.INSTANCE.getColorizer(ctx.caster)

            loc.map({
                IXplatAbstractions.INSTANCE.sendPacketNear(it, 128.0, ctx.world, MsgSingleParticleAck(it, colouriser))
            }, {
                if (it.isNotEmpty()) {
                    val first = it[0]

                    IXplatAbstractions.INSTANCE.sendPacketNear(first, 128.0, ctx.world, MsgParticleLinesAck(it, colouriser))
                }
            })
        }
    }
}