package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.iota.ItemIota

@Suppress("CAST_NEVER_SUCCEEDS")
class MishapStorageFull(val position: Vec3) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.RED)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("full_storage")

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        // get a random record from in the storage
        val storage = (ctx as IMixinCastingContext).boundStorage ?: return // somehow got mishap storage full with no storage, wild
        val allRecords = MediafiedItemManager.getAllRecords(storage) ?: return
        val index = allRecords.keys.random()
        val iota = ItemIota(index)
        val toDrop = iota.getStacksToDrop(iota.item.maxStackSize) // the stack to drop.

        //get a random pos within range
        var pos: Vec3? = null

        for (i in 1..10) {
            pos = position + Vec3(ctx.world.random.nextDouble(), ctx.world.random.nextDouble(), ctx.world.random.nextDouble())
            if (!ctx.world.getBlockState(BlockPos(pos)).isAir)
                pos = null
            else
                break
        }

        pos = pos ?: position

        // drop the selected stack at the randomly selected position.
        for (itemStack in toDrop) {
            ctx.world.addFreshEntity(ItemEntity(ctx.world, pos.x, pos.y, pos.z, itemStack))
        }
    }
}