package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.recipe.CopyProperties
import ram.talia.hexal.common.recipe.HexalRecipeSerializers

object OpFreeze : SpellOperator {
	const val FREEZE_COST = 3 * ManaConstants.DUST_UNIT

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val toFreeze = Vec3.atCenterOf(BlockPos(args.getChecked<Vec3>(0, argc)))

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
			val blockstate = ctx.world.getBlockState(pos)
			val fluidstate = ctx.world.getFluidState(pos)

			if (!ctx.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockstate, ctx.caster))
				return

			if (fluidstate.type == Fluids.WATER && blockstate.block is LiquidBlock) {
				ctx.world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState())
				return
			}
			if (fluidstate.type == Fluids.LAVA && blockstate.block is LiquidBlock) {
				ctx.world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())
				return
			}
			if (fluidstate.type == Fluids.FLOWING_LAVA && blockstate.block is LiquidBlock) {
				ctx.world.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState())
				return
			}

			val recman = ctx.world.recipeManager
			val recipes = recman.getAllRecipesFor(HexalRecipeSerializers.FREEZE_TYPE!!)

			val recipe = recipes.find{ it.matches(blockstate) }

			if (recipe != null)
				ctx.world.setBlockAndUpdate(pos, CopyProperties.copyProperties(blockstate, recipe.result))
		}
	}
}