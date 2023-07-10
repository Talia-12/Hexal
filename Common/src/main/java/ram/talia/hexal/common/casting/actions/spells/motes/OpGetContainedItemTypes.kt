
package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.nbt.CompoundTag
import ram.talia.hexal.api.casting.castables.UserDataConstMediaAction
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.casting.mishaps.MishapNoBoundStorage
import ram.talia.moreiotas.api.asActionResult

object OpGetContainedItemTypes : UserDataConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, userData: CompoundTag, env: CastingEnvironment): List<Iota> {
        val storage = if (userData.contains(MoteIota.TAG_TEMP_STORAGE))
                userData.getUUID(MoteIota.TAG_TEMP_STORAGE)
            else
                env.caster?.let { MediafiedItemManager.getBoundStorage(it) }
            ?: throw MishapNoBoundStorage()
        if (!MediafiedItemManager.isStorageLoaded(storage))
            throw MishapNoBoundStorage("storage_unloaded")

        return MediafiedItemManager.getAllContainedItemTypes(storage)?.toList()?.asActionResult ?: null.asActionResult
    }
}