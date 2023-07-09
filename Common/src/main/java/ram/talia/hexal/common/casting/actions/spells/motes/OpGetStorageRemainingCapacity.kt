@file:Suppress("CAST_NEVER_SUCCEEDS")

package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage

object OpGetStorageRemainingCapacity : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {

        val storageId = (env as IMixinCastingContext).boundStorage ?: throw MishapNoBoundStorage(env.caster.position())
        if (!MediafiedItemManager.isStorageLoaded(storageId))
            throw MishapNoBoundStorage(env.caster.position(), "storage_unloaded")
        val storage = MediafiedItemManager.getStorage(storageId)?.get() ?: throw MishapNoBoundStorage(env.caster.position())

        return (HexalConfig.server.maxRecordsInMediafiedStorage - storage.storedItems.size).asActionResult
    }
}