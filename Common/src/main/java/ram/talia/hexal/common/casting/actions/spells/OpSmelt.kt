package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SmeltingRecipe
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getBlockPosOrItemEntityOrItem
import ram.talia.hexal.api.spell.iota.ItemIota
import ram.talia.hexal.api.toIntCapped
import ram.talia.hexal.api.util.Anyone
import ram.talia.hexal.xplat.IXplatAbstractions
import java.util.*

object OpSmelt : SpellAction {
    override val argc = 1

    fun numToSmelt(toSmelt: Anyone<BlockPos, ItemEntity, ItemIota>): Int {
        return toSmelt.flatMap({ 1 }, { item -> item.item.count }, { item -> item.count.toIntCapped() })
    }

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val toSmelt = args.getBlockPosOrItemEntityOrItem(0, argc) ?: return null

        val pos = toSmelt.flatMap({ blockPos -> Vec3.atCenterOf(blockPos) }, { item -> item.position() }, { null })
        pos?.let { ctx.assertVecInRange(it) }

        val particles = mutableListOf<ParticleSpray>()

        if (pos != null)
            particles.add(ParticleSpray.burst(pos, 1.0))

        return Triple(
            Spell(toSmelt),
            HexalConfig.server.smeltCost * numToSmelt(toSmelt),
            particles
        )
    }

    private data class Spell(val vOrIeOrI: Anyone<BlockPos, ItemEntity, ItemIota>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            vOrIeOrI.map({ pos -> // runs this code if the player passed a BlockPos
                 if (!ctx.canEditBlockAt(pos)) return@map
                val blockState = ctx.world.getBlockState(pos)
                 if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(ctx.world, pos, blockState, ctx.caster)) return@map

                val itemStack = smeltResult(blockState.block.asItem(), ctx) ?: return@map

                if (itemStack.item is BlockItem) {
                    ctx.world.setBlockAndUpdate(pos, (itemStack.item as BlockItem).block.defaultBlockState())

                    if (itemStack.count > 1) {
                        itemStack.count -= 1
                        ctx.world.addFreshEntity(ItemEntity(ctx.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack.copy()))
                    }
                } else {
                    ctx.world.destroyBlock(pos, false, ctx.caster)
                    ctx.world.addFreshEntity(ItemEntity(ctx.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack.copy()))
                    // Send a block update, also copied from Ars Nouveau (this is all copied from Ars Nouveau)
                    if (!ctx.world.isOutsideBuildHeight(pos))
                        ctx.world.sendBlockUpdated(pos, ctx.world.getBlockState(pos), ctx.world.getBlockState(pos), 3) // don't know how this works
                }

            }, {itemEntity -> // runs this code if the player passed an ItemEntity
                val result = smeltResult(itemEntity.item.item, ctx) ?: return@map // cursed .item.item to map from ItemEntity to ItemLike to ItemStack

                result.count *= itemEntity.item.count

                ctx.world.addFreshEntity(ItemEntity(ctx.world, itemEntity.x, itemEntity.y, itemEntity.z, result.copy()))
                itemEntity.remove(Entity.RemovalReason.DISCARDED)
            }, {item -> // runs this code if the player passed a mote
                val result = smeltResult(item.item, ctx) ?: return@map

                item.templateOff(result, item.count * result.count)
            })
        }

        fun smeltResult(item: Item, ctx: CastingContext): ItemStack? {
            val optional: Optional<SmeltingRecipe> = ctx.world.recipeManager.getRecipeFor(
                    RecipeType.SMELTING, SimpleContainer(ItemStack(item, 1)),
                    ctx.world
            )

            if (!optional.isPresent) return null

            val result = optional.get().resultItem.copy()

            if (result.isEmpty) return null

            return result
        }
    }
}