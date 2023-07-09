package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.recipe.CopyProperties
import ram.talia.hexal.common.recipe.HexalRecipeTypes

object OpFreeze : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val toFreeze = Vec3.atCenterOf(BlockPos.containing(args.getVec3(0, argc)))

		env.assertVecInRange(toFreeze)

		return SpellAction.Result(
			Spell(toFreeze),
			HexalConfig.server.freezeCost,
			listOf(ParticleSpray.burst(toFreeze, 1.0))
		)
	}

	private data class Spell(val vec: Vec3) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {
			val pos = BlockPos.containing(vec)
			val blockState = env.world.getBlockState(pos)
			val fluidState = env.world.getFluidState(pos)

			if (!env.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, pos, blockState, env.caster))
				return

			if (fluidState.type == Fluids.WATER && blockState.block is LiquidBlock) {
				env.world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState())
				return
			}
			if (fluidState.type == Fluids.LAVA && blockState.block is LiquidBlock) {
				env.world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())
				return
			}
			if (fluidState.type == Fluids.FLOWING_LAVA && blockState.block is LiquidBlock) {
				env.world.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState())
				return
			}

			val recman = env.world.recipeManager
			val recipes = recman.getAllRecipesFor(HexalRecipeTypes.FREEZE_TYPE)

			val recipe = recipes.find{ it.matches(blockState) }

			if (recipe != null)
				env.world.setBlockAndUpdate(pos, CopyProperties.copyProperties(blockState, recipe.result))
		}
	}
}