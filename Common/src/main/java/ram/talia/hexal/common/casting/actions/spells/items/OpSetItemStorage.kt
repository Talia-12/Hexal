package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.getItem
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage

/**
 * Moves the ItemRecord pointed to by an item iota to a different MediafiedStorage. **Invalidates all previous item iotas pointing to that ItemRecord**.
 */
object OpSetItemStorage : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val item = args.getItem(0, argc) ?: return null.asActionResult
        val storagePos = args.getBlockPos(1, argc)

        ctx.assertVecInRange(storagePos)

        val storage = ctx.world.getBlockEntity(storagePos) as? BlockEntityMediafiedStorage ?: throw MishapInvalidIota.ofType(args[1], 0, "mediafied_storage")

        val newItem = item.setStorage(storage.uuid)

        return newItem?.let { listOf(it) } ?: null.asActionResult
    }
}