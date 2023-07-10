
package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.nbt.CompoundTag
import ram.talia.hexal.api.casting.castables.UserDataConstMediaAction
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.casting.mishaps.MishapNoBoundStorage

object OpGetStorageRemainingCapacity : UserDataConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, userData: CompoundTag, env: CastingEnvironment): List<Iota> {

        val storageId = if (userData.contains(MoteIota.TAG_TEMP_STORAGE))
                userData.getUUID(MoteIota.TAG_TEMP_STORAGE)
            else
                env.caster?.let { MediafiedItemManager.getBoundStorage(it) }
            ?: throw MishapNoBoundStorage()
        if (!MediafiedItemManager.isStorageLoaded(storageId))
            throw MishapNoBoundStorage("storage_unloaded")
        val storage = MediafiedItemManager.getStorage(storageId)?.get() ?: throw MishapNoBoundStorage()

        return (HexalConfig.server.maxRecordsInMediafiedStorage - storage.storedItems.size).asActionResult
    }
}