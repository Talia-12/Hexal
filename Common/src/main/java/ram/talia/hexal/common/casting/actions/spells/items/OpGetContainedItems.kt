package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asActionResult
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.getItemOrItemType
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager

object OpGetContainedItems : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val item = args.getItemOrItemType(0, argc) ?: return null.asActionResult

        val results = item.map({itemIota ->
            itemIota.record?.let { MediafiedItemManager.getItemRecordsMatching(ctx.caster, it) }
        }, {
            MediafiedItemManager.getItemRecordsMatching(ctx.caster, it)
        }) ?: return null.asActionResult

        return results.asActionResult
    }
}