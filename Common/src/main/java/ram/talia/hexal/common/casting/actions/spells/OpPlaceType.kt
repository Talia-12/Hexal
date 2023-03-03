package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.DiscoveryHandlers
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getBlockType
import java.util.function.Predicate

object OpPlaceType : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val block = args.getBlockType(0, argc) ?:
            throw MishapInvalidIota.ofType(args[0], 1, "type.block.able")
        val pos = args.getBlockPos(1, argc)

        ctx.assertVecInRange(pos)

        // Mishap if pos already contains a block that can't be replaced
        val blockHit = BlockHitResult(
                Vec3.atCenterOf(pos), ctx.caster.direction, pos, false
        )
        val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
        val placeContext = BlockPlaceContext(itemUseCtx)

        val worldState = ctx.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return Triple(
                Spell(pos, block),
                HexalConfig.server.placeTypeCost,
                listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val pos: BlockPos, val block: Block) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.canEditBlockAt(pos))
                return

            val blockHit = BlockHitResult(
                    Vec3.atCenterOf(pos), ctx.caster.direction, pos, false
            )

            val bstate = ctx.world.getBlockState(pos)
            val placeeStack = getItemSlot(ctx) { it.item is BlockItem && (it.item as BlockItem).block == block }?.copy() ?: return

            if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, placeeStack, ctx.caster))
                return

            if (!placeeStack.isEmpty) {
                // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                val oldStack = ctx.caster.getItemInHand(ctx.castingHand)
                val spoofedStack = placeeStack.copy()

                // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                spoofedStack.count = 1
                ctx.caster.setItemInHand(ctx.castingHand, spoofedStack)

                val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
                val placeContext = BlockPlaceContext(itemUseCtx)
                if (bstate.canBeReplaced(placeContext)) {
                    if (ctx.withdrawItem(placeeStack, 1, false)) {
                        val res = spoofedStack.useOn(placeContext)

                        ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                        if (res != InteractionResult.FAIL) {
                            ctx.withdrawItem(placeeStack, 1, true)

                            ctx.world.playSound(
                                    ctx.caster,
                                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                                    1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                            )
                            val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                            ctx.world.sendParticles(
                                    particle, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    4, 0.1, 0.2, 0.1, 0.1
                            )
                        }
                    } else {
                        ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                    }
                } else {
                    ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                }
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