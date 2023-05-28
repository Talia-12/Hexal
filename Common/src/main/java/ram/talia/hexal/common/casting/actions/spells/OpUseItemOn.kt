package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.ktxt.UseOnContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.mojang.brigadier.Message
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.getItem
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.VarargSpellAction
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.iota.ItemIota
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

object OpUseItemOn : VarargSpellAction {
    override fun argc(stack: List<Iota>): Int {
        return if (stack[0] is EntityIota) 2 else 3
    }

    override fun execute(
            args: List<Iota>,
            argc: Int,
            ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val item = args.getItem(0, argc)

        if ((item == null) || ((item.count != 1L) && (item.tag != null) ))
        {
            throw MishapInvalidIota(item!!, 0, "hexcasting.mishap.invalid_value.mote_with_nbt_not_size_one".asTranslatedComponent)
        }

        if (argc == 2) {
            // Entity Version
            val target = args.getEntity(1, argc)

            ctx.assertEntityInRange(target)

            val storage = (ctx as IMixinCastingContext).boundStorage ?: item.itemIndex.storage
            if (!MediafiedItemManager.isStorageLoaded(storage))
                throw MishapNoBoundStorage(ctx.caster.position(), "storage_unloaded")

            return Triple(
                EntityTargetSpell(target, item),
                MediaConstants.DUST_UNIT,
                listOf(ParticleSpray.burst(Vec3.atCenterOf(target.onPos), 1.0))
            )
        }
        else {
            // Block Version
            val direction = args.getVec3(2, argc)
            val target = args.getBlockPos(1, argc)

            val storage = (ctx as IMixinCastingContext).boundStorage ?: item.itemIndex.storage
            if (!MediafiedItemManager.isStorageLoaded(storage))
                throw MishapNoBoundStorage(ctx.caster.position(), "storage_unloaded")

            return Triple(
                BlockTargetSpell(target, direction, item),
                MediaConstants.DUST_UNIT,
                listOf(ParticleSpray.burst(Vec3.atCenterOf(BlockPos(target)), 1.0))
            )
        }
    }

    private data class EntityTargetSpell(val entity: Entity, val item: ItemIota) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.isEntityInRange(entity))
                return

            val itemStack = ItemStack(item.item, 1)
            itemStack.tag = item.tag;

            // Swap item in hand to the new stack
            val oldStack = ctx.caster.getItemInHand(ctx.castingHand)
            ctx.caster.setItemInHand(ctx.castingHand, itemStack)

            entity.interact(ctx.caster, InteractionHand.MAIN_HAND)

            // Swap back to the old item
            ctx.caster.setItemInHand(ctx.castingHand, oldStack)

            item.tag = itemStack.tag
            if (itemStack.isEmpty)
            {
                item.removeItems(1)
            }
        }
    }

    private data class BlockTargetSpell(val pos: BlockPos, val direction: Vec3, val item: ItemIota) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!ctx.canEditBlockAt(pos))
                return

            val itemStack = ItemStack(item.item, 1)
            itemStack.tag = item.tag;

            val context = UseOnContext(
                ctx.world,
                ctx.caster,
                InteractionHand.MAIN_HAND,
                itemStack,
                BlockHitResult(Vec3.atCenterOf(pos), Direction.getNearest(direction.x, direction.y, direction.z), pos, false)
            )

            val isAllowed = IXplatAbstractions.INSTANCE.isPlacingAllowed(ctx.world, pos, itemStack, ctx.caster)
            if (!isAllowed)
            {
                return
            }
            itemStack.useOn(context).consumesAction()
            item.tag = itemStack.tag

            if (itemStack.isEmpty)
            {
                item.removeItems(1)
            }
        }
    }
}