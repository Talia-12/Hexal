package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.casting.wisp.IMixinCastingContext

/**
 * Tick Acceleration!
 */
object OpTick : SpellAction {
    override val argc = 1

    fun costFromTimesTicked(timesTicked: Int): Int {
        return HexalConfig.server.tickConstantCost + HexalConfig.server.tickCostPerTicked * timesTicked
    }

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val pos = args.getBlockPos(0, argc)
        val ictx = env as IMixinCastingContext

        env.assertVecInRange(pos.center)

        val cost = costFromTimesTicked(ictx.getTimesTicked(pos))

        return SpellAction.Result(
                Spell(pos),
                cost,
                listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0, 5))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val ictx = env as IMixinCastingContext
            ictx.incTimesTicked(pos)

            // https://github.com/haoict/time-in-a-bottle/blob/1.19/src/main/java/com/haoict/tiab/entities/TimeAcceleratorEntity.java
            val blockState = env.world.getBlockState(pos)
            val level: ServerLevel = env.world
            val targetBE = level.getBlockEntity(pos)

            if (!HexalConfig.server.isAccelerateAllowed(BuiltInRegistries.BLOCK.getKey(blockState.block)))
                return

            if (targetBE != null) {
                // if is TileEntity (furnace, brewing stand, ...)
                val ticker = targetBE.blockState.getTicker(level, targetBE.type as BlockEntityType<BlockEntity>)
                ticker?.tick(level, pos, targetBE.blockState, targetBE)

            } else if (blockState.isRandomlyTicking) {
                // if is random ticket block (grass block, sugar cane, wheat or sapling, ...)
                if (level.random.nextInt(HexalConfig.server.tickRandomTickIProb) == 0) {
                    blockState.randomTick(level, pos, level.random)
                }
            }
        }
    }

}