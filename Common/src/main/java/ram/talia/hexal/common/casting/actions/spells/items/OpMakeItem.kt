package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getItemEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.config.HexalConfig

/**
 * Mediafy an ItemEntity.
 */
object OpMakeItem : ConstMediaAction { // TODO: Convert to SpellAction or just Action so that I can do particle effects.
    override val argc = 1

    override val mediaCost: Int
        get() = HexalConfig.server.makeItemCost

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val iEntity = args.getItemEntity(0, argc)

        ctx.assertEntityInRange(iEntity)

        val stack = iEntity.item

        iEntity.discard()

        return stack.asActionResult
    }
}