@file:Suppress("CAST_NEVER_SUCCEEDS")

package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import ram.talia.hexal.api.getItem
import ram.talia.hexal.api.getStrictlyPositiveInt
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

object OpSplitItem : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val item = args.getItem(0, argc) ?: return listOf(NullIota())
        val toSplitOff = args.getStrictlyPositiveInt(1, argc)

        val storage = (ctx as IMixinCastingContext).boundStorage ?: item.itemIndex.storage
        if (MediafiedItemManager.isStorageFull(storage) != false) // if this is somehow null we should still throw an error here, things have gone pretty wrong
            throw MishapStorageFull(ctx.caster.position())

        val split = item.splitOff(toSplitOff, storage) ?: return listOf(item.copy(), NullIota())
        return listOf(item.copy(), split.copy())
    }
}