package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.common.recipe.CopyProperties
import ram.talia.hexal.common.recipe.HexalRecipeTypes

object OpFreeze : SpellAction {
	const val FREEZE_COST = 3 * MediaConstants.DUST_UNIT

	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val toFreeze = Vec3.atCenterOf(BlockPos(args.getVec3(0, argc)))

		ctx.assertVecInRange(toFreeze)

		return Triple(
			Spell(toFreeze),
			FREEZE_COST,
			listOf(ParticleSpray.burst(toFreeze, 1.0))
		)
	}

	private data class Spell(val vec: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			val pos = BlockPos(vec)
			val blockState = ctx.world.getBlockState(pos)
			val fluidState = ctx.world.getFluidState(pos)

			if (!ctx.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockState, ctx.caster))
				return

			if (fluidState.type == Fluids.WATER && blockState.block is LiquidBlock) {
				ctx.world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState())
				return
			}
			if (fluidState.type == Fluids.LAVA && blockState.block is LiquidBlock) {
				ctx.world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())
				return
			}
			if (fluidState.type == Fluids.FLOWING_LAVA && blockState.block is LiquidBlock) {
				ctx.world.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState())
				return
			}

			val recman = ctx.world.recipeManager
			val recipes = recman.getAllRecipesFor(HexalRecipeTypes.FREEZE_TYPE)

			val recipe = recipes.find{ it.matches(blockState) }

			if (recipe != null)
				ctx.world.setBlockAndUpdate(pos, CopyProperties.copyProperties(blockState, recipe.result))
		}
	}
}