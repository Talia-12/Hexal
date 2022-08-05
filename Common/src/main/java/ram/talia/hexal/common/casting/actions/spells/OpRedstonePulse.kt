package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.DiodeBlock
import net.minecraft.world.level.redstone.Redstone
import net.minecraft.world.phys.Vec3

object OpRedstonePulse : SpellOperator {
    private const val COST_PULSE = (0.25 * ManaConstants.DUST_UNIT).toInt()

    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val pos = args.getChecked<Vec3>(0, argc)

        ctx.assertVecInRange(pos)

        val centred = Vec3.atCenterOf(BlockPos(pos))
        return Triple(
            Spell(pos),
            COST_PULSE,
            listOf(ParticleSpray.cloud(centred, 0.5, 1))
        )
    }

    private data class Spell(val pos: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val blockPos = BlockPos(pos)

            if (!ctx.world.mayInteract(ctx.caster, blockPos))
                return

            val blockState = ctx.world.getBlockState(blockPos)

            blockState.setValue(DiodeBlock.POWERED, true)
        }
    }
}