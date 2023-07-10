package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig

/**
 * Tick Acceleration!
 */
object OpTick : SpellAction {
    override val argc = 1

    const val TAG_TIMES_TICKED = "hexal:times_ticked"

    private fun costFromTimesTicked(timesTicked: Int): Int {
        return HexalConfig.server.tickConstantCost + HexalConfig.server.tickCostPerTicked * timesTicked
    }

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        throw IllegalStateException("call executeWithUserdata instead.")
    }

    override fun executeWithUserdata(args: List<Iota>, env: CastingEnvironment, userData: CompoundTag): SpellAction.Result {
        val pos = args.getBlockPos(0, argc)

        env.assertVecInRange(pos.center)

        val timesTicked = userData.getCompound(TAG_TIMES_TICKED).getInt(pos.toShortString())

        val cost = costFromTimesTicked(timesTicked)

        return SpellAction.Result(
            Spell(pos),
            cost,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0, 5))
        )
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            throw IllegalStateException("call cast(env, image) instead.")
        }

        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage {
            val userData = image.userData.copy()
            val timesTickedMap = userData.getCompound(TAG_TIMES_TICKED)
            timesTickedMap.putInt(pos.toShortString(), timesTickedMap.getInt(pos.toShortString()) + 1)
            val newImage = image.copy(userData = userData)

            // https://github.com/haoict/time-in-a-bottle/blob/1.19/src/main/java/com/haoict/tiab/entities/TimeAcceleratorEntity.java
            val blockState = env.world.getBlockState(pos)
            val level: ServerLevel = env.world
            val targetBE = level.getBlockEntity(pos)

            if (!HexalConfig.server.isAccelerateAllowed(BuiltInRegistries.BLOCK.getKey(blockState.block)))
                return newImage

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

            return newImage
        }
    }
}