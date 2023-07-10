package ram.talia.hexal.fabric.interop.phantom

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.fabric.network.MsgPhaseBlockS2C
import ram.talia.hexal.xplat.IXplatAbstractions

object OpPhaseBlock : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val pos = args.getBlockPos(0, argc)
        val time = args.getPositiveDouble(1, argc)

        ctx.assertVecInRange(Vec3.atCenterOf(pos))

        val bs: BlockState = ctx.world.getBlockState(pos)
        if (bs.getDestroySpeed(ctx.world, pos) < 0.0f)
            throw MishapInvalidIota.of(args[1], 0, "unbreakable_block", pos)

        return SpellAction.Result(
            Spell(pos, (time * 20).toInt()),
            (HexalConfig.server.phaseBlockCostFactor * time * time).toInt(),
            listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 0.5))
        )
    }

    private data class Spell(val pos: BlockPos, val ticks: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val bs: BlockState = env.world.getBlockState(pos)
            if (bs.getDestroySpeed(env.world, pos) < 0.0f)
                return

            env.world.phaseBlock(pos, ticks)

            IXplatAbstractions.INSTANCE.sendPacketTracking(pos, env.world, MsgPhaseBlockS2C(pos, ticks))
        }
    }
}