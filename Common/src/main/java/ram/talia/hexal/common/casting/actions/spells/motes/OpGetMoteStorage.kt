package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asActionResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager

/**
 * Get the storage an item is contained in.
 */
object OpGetMoteStorage : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val item = args.getMote(0, argc) ?: return null.asActionResult
        // get the position of the MediafiedStorage that the item is contained in, return it.
        return MediafiedItemManager.getStorage(item.itemIndex.storage)?.get()?.pos?.let { Vec3.atCenterOf(it) }?.asActionResult ?: null.asActionResult
    }
}