@file:Suppress("CAST_NEVER_SUCCEEDS")

package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.getBlockPosOrNull
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage

object OpGetStorageCurrentFill : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val pos = args.getBlockPosOrNull(0, argc)

        pos?.let { ctx.assertVecInRange(it) }

        val storage = if (pos == null) {
            (ctx as IMixinCastingContext).boundStorage?.let { MediafiedItemManager.getStorage(it)?.get() } ?: throw MishapNoBoundStorage(ctx.caster.position())
        }  else {
            ctx.world.getBlockEntity(pos) as? BlockEntityMediafiedStorage ?: throw MishapInvalidIota.ofType(args[1], 0, "mediafied_storage")
        }

        return storage.storedItems.size.asActionResult
    }
}