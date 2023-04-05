package ram.talia.hexal.fabric.interop.phantom

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.fabric.network.MsgPhaseBlockAck
import ram.talia.hexal.xplat.IXplatAbstractions

object OpPhaseBlock : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getBlockPos(0, argc)
        val time = args.getPositiveDouble(1, argc)

        ctx.assertVecInRange(Vec3.atCenterOf(pos))

        val bs: BlockState = ctx.world.getBlockState(pos)
        if (bs.getDestroySpeed(ctx.world, pos) < 0.0f)
            throw MishapInvalidIota.of(args[1], 0, "unbreakable_block", pos)

        return Triple(
            Spell(pos, (time * 20).toInt()),
            (HexalConfig.server.phaseBlockCostFactor * time * time).toInt(),
            listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 0.5))
        )
    }

    private data class Spell(val pos: BlockPos, val ticks: Int) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val bs: BlockState = ctx.world.getBlockState(pos)
            if (bs.getDestroySpeed(ctx.world, pos) < 0.0f)
                return

            ctx.world.phaseBlock(pos, ticks)

            IXplatAbstractions.INSTANCE.sendPacketTracking(pos, ctx.world, MsgPhaseBlockAck(pos, ticks))
        }
    }
}