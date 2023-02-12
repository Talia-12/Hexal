package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
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
import ram.talia.hexal.api.config.HexalConfig
import kotlin.math.min

// https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/common/item/lens/LensWeight.java
object OpFallingBlock : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val pos = args.getVec3(0, argc)
		ctx.assertVecInRange(pos)

		val centered = Vec3.atCenterOf(BlockPos(pos))
		return Triple(
			Spell(pos),
			HexalConfig.server.fallingBlockCost,
			listOf(ParticleSpray.burst(centered, 1.0))
		)
	}

	private data class Spell(val v: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			val pos = BlockPos(v)

			val blockstate = ctx.world.getBlockState(pos)
			if (!ctx.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockstate, ctx.caster))
				return

			val tier = HexConfig.server().opBreakHarvestLevel()

			val stateBelow = ctx.world.getBlockState(pos.below())

			if ((
					FallingBlock.isFree(stateBelow)
					|| !stateBelow.canOcclude()
					|| stateBelow.`is`(BlockTags.SLABS)
				)
				&& !blockstate.isAir
				&& blockstate.getDestroySpeed(ctx.world, pos) >= 0f // fix being able to break bedrock &c
				&& ctx.world.getBlockEntity(pos) == null
				&& IXplatAbstractions.INSTANCE.isCorrectTierForDrops(tier, blockstate)
				&& canSilkTouch(ctx.world, pos, blockstate, tier.level, ctx.caster)
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

		fun canSilkTouch(level: ServerLevel, pos: BlockPos, state: BlockState, harvestLevel: Int, owner: Entity?): Boolean {
			val harvestToolStack: ItemStack = getHarvestToolStack(harvestLevel, state)
			if (harvestToolStack.isEmpty) {
				return false
			}
			harvestToolStack.enchant(Enchantments.SILK_TOUCH, 1)
			val drops: List<ItemStack> = Block.getDrops(state, level, pos, null, owner, harvestToolStack)
			val blockItem: Item = state.block.asItem()
			return drops.any { s -> s.item === blockItem }
		}

		companion object {
			fun getHarvestToolStack(harvestLevel: Int, state: BlockState): ItemStack {
				return getTool(harvestLevel, state).copy()
			}

			private fun getTool(harvestLevel: Int, state: BlockState): ItemStack {
				val idx = min(harvestLevel, HARVEST_TOOLS_BY_LEVEL.size - 1)
				if (!state.requiresCorrectToolForDrops()) {
					return HARVEST_TOOLS_BY_LEVEL[idx][0]
				}
				for (tool in HARVEST_TOOLS_BY_LEVEL[idx]) {
					if (tool.isCorrectToolForDrops(state)) {
						return tool
					}
				}
				return ItemStack.EMPTY
			}

			private val HARVEST_TOOLS_BY_LEVEL: List<List<ItemStack>> = listOf(
				stacks(Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL),
				stacks(Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE, Items.STONE_SHOVEL),
				stacks(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SHOVEL),
				stacks(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_SHOVEL),
				stacks(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE, Items.NETHERITE_SHOVEL)
			)

			private fun stacks(vararg items: Item): List<ItemStack> {
				return items.map { item -> ItemStack(item) }
			}
		}
	}
}