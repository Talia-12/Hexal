@file:Suppress("CAST_NEVER_SUCCEEDS")

package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage

object OpGetStorageRemainingCapacity : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {

        val storageId = (ctx as IMixinCastingContext).boundStorage ?: throw MishapNoBoundStorage(ctx.caster.position())
        if (!MediafiedItemManager.isStorageLoaded(storageId))
            throw MishapNoBoundStorage(ctx.caster.position(), "storage_unloaded")
        val storage = MediafiedItemManager.getStorage(storageId)?.get() ?: throw MishapNoBoundStorage(ctx.caster.position())

        return (HexalConfig.server.maxRecordsInMediafiedStorage - storage.storedItems.size).asActionResult
    }
}