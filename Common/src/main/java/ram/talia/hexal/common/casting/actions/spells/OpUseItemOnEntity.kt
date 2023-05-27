package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.ktxt.UseOnContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.getItem
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.iota.ItemIota
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

object OpUseItemOnEntity : SpellAction {
    override val argc = 2
    override fun execute(
            args: List<Iota>,
            ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(1, argc)
        val item = args.getItem(0, argc)
        ctx.assertEntityInRange(target)

        if (item == null)
        {
            throw MishapInvalidIota.of(NullIota(), 0, "item_cannot_be_null")
        }

        val storage = (ctx as IMixinCastingContext).boundStorage ?: item.itemIndex.storage
        if (!MediafiedItemManager.isStorageLoaded(storage))
            throw MishapNoBoundStorage(ctx.caster.position(), "storage_unloaded")
        if (MediafiedItemManager.isStorageFull(storage) != false) // if this is somehow null we should still throw an error here, things have gone pretty wrong
            throw MishapStorageFull(ctx.caster.position())

        return Triple(
                Spell(target, item),
                MediaConstants.DUST_UNIT,
                listOf(ParticleSpray.burst(Vec3.atCenterOf(target.onPos), 1.0))
        )
    }

    private data class Spell(val entity: Entity, val item: ItemIota) : RenderedSpell {
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
}