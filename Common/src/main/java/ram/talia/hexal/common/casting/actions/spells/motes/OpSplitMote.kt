package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.nbt.CompoundTag
import ram.talia.hexal.api.casting.castables.UserDataConstMediaAction
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.casting.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.casting.mishaps.MishapStorageFull
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.getStrictlyPositiveLong
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager

object OpSplitMote : UserDataConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, userData: CompoundTag, env: CastingEnvironment): List<Iota> {
        val item = args.getMote(0, argc) ?: return listOf(NullIota())
        val toSplitOff = args.getStrictlyPositiveLong(1, argc)

        val storage = if (userData.contains(MoteIota.TAG_TEMP_STORAGE))
                userData.getUUID(MoteIota.TAG_TEMP_STORAGE)
            else
                env.caster?.let { MediafiedItemManager.getBoundStorage(it) }
            ?: throw MishapNoBoundStorage()
        if (!MediafiedItemManager.isStorageLoaded(storage))
            throw MishapNoBoundStorage("storage_unloaded")
        if (MediafiedItemManager.isStorageFull(storage) != false) // if this is somehow null we should still throw an error here, things have gone pretty wrong
            throw MishapStorageFull(storage)

        val split = item.splitOff(toSplitOff, storage) ?: return listOf(item.copy(), NullIota())
        return listOf(item.copy(), split.copy())
    }
}