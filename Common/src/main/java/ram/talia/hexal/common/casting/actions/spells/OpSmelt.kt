package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
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
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.toIntCapped
import ram.talia.hexal.api.util.Anyone
import ram.talia.hexal.xplat.IXplatAbstractions
import java.util.*

object OpSmelt : SpellAction {
    override val argc = 1

    fun numToSmelt(toSmelt: Anyone<BlockPos, ItemEntity, MoteIota>): Int {
        return toSmelt.flatMap({ 1 }, { item -> item.item.count }, { item -> item.count.toIntCapped() })
    }

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val toSmelt = args.getBlockPosOrItemEntityOrItem(0, argc)

        val pos = toSmelt.flatMap({ blockPos -> Vec3.atCenterOf(blockPos) }, { item -> item.position() }, { null })
        pos?.let { env.assertVecInRange(it) }

        val particles = mutableListOf<ParticleSpray>()

        if (pos != null)
            particles.add(ParticleSpray.burst(pos, 1.0))

        return SpellAction.Result(
            Spell(toSmelt),
            HexalConfig.server.smeltCost * numToSmelt(toSmelt),
            particles
        )
    }

    private data class Spell(val vOrIeOrI: Anyone<BlockPos, ItemEntity, MoteIota>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            vOrIeOrI.map({ pos -> // runs this code if the player passed a BlockPos
                 if (!env.canEditBlockAt(pos)) return@map
                val blockState = env.world.getBlockState(pos)
                 if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, pos, blockState, env.caster)) return@map

                val itemStack = smeltResult(blockState.block.asItem(), env) ?: return@map

                if (itemStack.item is BlockItem) {
                    env.world.setBlockAndUpdate(pos, (itemStack.item as BlockItem).block.defaultBlockState())

                    if (itemStack.count > 1) {
                        itemStack.count -= 1
                        env.world.addFreshEntity(ItemEntity(env.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack.copy()))
                    }
                } else {
                    env.world.destroyBlock(pos, false, env.caster)
                    env.world.addFreshEntity(ItemEntity(env.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack.copy()))
                    // Send a block update, also copied from Ars Nouveau (this is all copied from Ars Nouveau)
                    if (!env.world.isOutsideBuildHeight(pos))
                        env.world.sendBlockUpdated(pos, env.world.getBlockState(pos), env.world.getBlockState(pos), 3) // don't know how this works
                }

            }, {itemEntity -> // runs this code if the player passed an ItemEntity
                val result = smeltResult(itemEntity.item.item, env) ?: return@map // cursed .item.item to map from ItemEntity to ItemLike to ItemStack

                result.count *= itemEntity.item.count

                env.world.addFreshEntity(ItemEntity(env.world, itemEntity.x, itemEntity.y, itemEntity.z, result.copy()))
                itemEntity.remove(Entity.RemovalReason.DISCARDED)
            }, {item -> // runs this code if the player passed a mote
                val result = smeltResult(item.item, env) ?: return@map

                item.templateOff(result, item.count * result.count)
            })
        }

        fun smeltResult(item: Item, env: CastingEnvironment): ItemStack? {
            val optional: Optional<SmeltingRecipe> = env.world.recipeManager.getRecipeFor(
                    RecipeType.SMELTING, SimpleContainer(ItemStack(item, 1)),
                    env.world
            )

            if (!optional.isPresent) return null

            val result = optional.get().getResultItem(env.world.registryAccess()).copy()

            if (result.isEmpty) return null

            return result
        }
    }
}