package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import com.mojang.datafixers.util.Either
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.*
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.network.MsgParticleLinesAck
import ram.talia.hexal.common.network.MsgSingleParticleAck
import ram.talia.hexal.xplat.IXplatAbstractions

object OpParticles : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val loc = args.getVec3OrListVec3(0, argc)

        // assert all locs in ambit.
        loc.map({ env.assertVecInRange(it) }, { env.assertVecListInRange(it, 32.0) })

        return SpellAction.Result(
                Spell(loc),
                loc.map({ HexalConfig.server.particlesCost }, { it.size * HexalConfig.server.particlesCost }),
                listOf()
        )
    }

    data class Spell(val loc: Either<Vec3, List<Vec3>>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val colouriser = at.petrak.hexcasting.xplat.IXplatAbstractions.INSTANCE.getPigment(env.caster)

            loc.map({
                IXplatAbstractions.INSTANCE.sendPacketNear(it, 128.0, env.world, MsgSingleParticleAck(it, colouriser))
            }, {
                if (it.isNotEmpty()) {
                    val first = it[0]

                    IXplatAbstractions.INSTANCE.sendPacketNear(first, 128.0, env.world, MsgParticleLinesAck(it, colouriser))
                }
            })
        }
    }
}