package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.math.min

// https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/common/item/lens/LensWeight.java
object OpFallingBlock : SpellOperator {
	private const val COST = (ManaConstants.DUST_UNIT * 1.5).toInt()

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val pos = args.getChecked<Vec3>(0, argc)
		ctx.assertVecInRange(pos)

		val centered = Vec3.atCenterOf(BlockPos(pos))
		return Triple(
			Spell(pos),
			COST,
			listOf(ParticleSpray.burst(centered, 1.0))
		)
	}

	private data class Spell(val v: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			val pos = BlockPos(v)

			if (!ctx.canEditBlockAt(pos))
				return

			val blockstate = ctx.world.getBlockState(pos)
			if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockstate, ctx.caster))
				return

			val tier = HexConfig.server().opBreakHarvestLevel()

			if (
				FallingBlock.isFree(ctx.world.getBlockState(pos.below()))
				&& !blockstate.isAir
				&& blockstate.getDestroySpeed(ctx.world, pos) >= 0f // fix being able to break bedrock &c
				&& IXplatAbstractions.INSTANCE.isCorrectTierForDrops(tier, blockstate)
			) {
				val falling: FallingBlockEntity = FallingBlockEntity.fall(ctx.world, pos, blockstate)
				falling.time = 1
				ctx.world.sendParticles(
					BlockParticleOption(ParticleTypes.FALLING_DUST, blockstate),
					pos.x + 0.5,
					pos.y + 0.5,
					pos.z + 0.5,
					10,
					0.45,
					0.45,
					0.45,
					5.0
				)
			}
		}
	}
}