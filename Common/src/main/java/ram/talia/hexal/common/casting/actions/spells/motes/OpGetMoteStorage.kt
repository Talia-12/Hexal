package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager

/**
 * Get the storage an item is contained in.
 */
object OpGetMoteStorage : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val item = args.getMote(0, argc) ?: return null.asActionResult
        // get the position of the MediafiedStorage that the item is contained in, return it.
        return MediafiedItemManager.getStorage(item.itemIndex.storage)?.get()?.pos?.let { Vec3.atCenterOf(it) }?.asActionResult ?: null.asActionResult
    }
}