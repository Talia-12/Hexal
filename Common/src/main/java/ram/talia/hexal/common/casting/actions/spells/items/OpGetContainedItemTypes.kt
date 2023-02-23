package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asActionResult
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager

object OpGetContainedItemTypes : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return MediafiedItemManager.getAllContainedItemTypes(ctx.caster)?.toList()?.asActionResult ?: null.asActionResult
    }
}