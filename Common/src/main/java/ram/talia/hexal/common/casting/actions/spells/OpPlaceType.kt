package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.DiscoveryHandlers
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getBlockTypeOrBlockItem
import ram.talia.hexal.api.spell.iota.MoteIota
import java.util.function.Predicate

object OpPlaceType : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val block = args.getBlockTypeOrBlockItem(0, argc) ?:
            throw MishapInvalidIota.ofType(args[0], 1, "type.block.able")
        val pos = args.getBlockPos(1, argc)

        env.assertVecInRange(pos.center)

        // Mishap if pos already contains a block that can't be replaced
        val blockHit = BlockHitResult(
                Vec3.atCenterOf(pos), env.caster?.direction ?: Direction.UP, pos, false
        )
        val itemUseCtx = UseOnContext(env.world, env.caster, env.castingHand, env.caster?.mainHandItem ?: ItemStack(Items.COBBLESTONE), blockHit)
        val placeContext = BlockPlaceContext(itemUseCtx)

        val worldState = env.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return SpellAction.Result(
                Spell(pos, block),
                HexalConfig.server.placeTypeCost,
                listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos, val blockOrMoteIota: Either<Block, MoteIota>) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!env.canEditBlockAt(pos))
                return

            val blockHit = BlockHitResult(
                    Vec3.atCenterOf(pos), env.caster?.direction ?: Direction.UP, pos, false
            )

            val bstate = env.world.getBlockState(pos)
            val placeeStack = blockOrMoteIota.map(
                    { block -> getItemSlot(env) { it.item is BlockItem && (it.item as BlockItem).block == block }?.copy() },
                    { itemIota -> if (itemIota.item is BlockItem) itemIota.record?.toStack()?.takeUnless { it.isEmpty } else null }
            )  ?: return

            if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(env.world, pos, placeeStack, env.caster))
                return

            if (!placeeStack.isEmpty) {
                // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                val oldStack = env.caster.getItemInHand(env.castingHand)
                val spoofedStack = placeeStack.copy()

                // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                spoofedStack.count = 1
                env.caster.setItemInHand(env.castingHand, spoofedStack)

                val itemUseCtx = UseOnContext(env.caster, env.castingHand, blockHit)
                val placeContext = BlockPlaceContext(itemUseCtx)

                if (!bstate.canBeReplaced(placeContext)) {
                    env.caster.setItemInHand(env.castingHand, oldStack)
                    return
                }

                if (blockOrMoteIota.left().isPresent && !env.withdrawItem(placeeStack, 1, false)) {
                    env.caster.setItemInHand(env.castingHand, oldStack)
                    return
                }

                val res = spoofedStack.useOn(placeContext)

                env.caster.setItemInHand(env.castingHand, oldStack)

                if (res == InteractionResult.FAIL)
                    return

                blockOrMoteIota.map(
                        { env.withdrawItem(placeeStack, 1, true) }, // if we're placing based on a block type, remove from the caster's inventory
                        { itemIota -> itemIota.removeItems(1) } // if we're placing from an item iota, remove from the iota.
                )

                env.world.playSound(
                        env.caster,
                        pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                        bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                        1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                )
                val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                env.world.sendParticles(
                        particle, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                        4, 0.1, 0.2, 0.1, 0.1
                )
            }
        }

        fun getItemSlot(ctx: CastingContext, stackOK: Predicate<ItemStack>): ItemStack? {
            val items = DiscoveryHandlers.collectItemSlots(ctx)

            for (stack in items) {
                if (stackOK.test(stack)) {
                    return stack
                }
            }
            return null
        }
    }
}