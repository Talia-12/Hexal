package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

class SerialisedIota(var tag: CompoundTag?) {
    fun set(iota: SpellDatum<*>) {
        tag = iota.serializeToNBT()
    }

    fun get(level: ServerLevel): SpellDatum<*>? = tag?.let { SpellDatum.fromNBT(it, level) }

    fun copy(serIota: SerialisedIota) {
        tag = serIota.tag?.copy()
    }
}

class SerialisedIotaList(var tag: ListTag?) {
    fun set(iotas: List<SpellDatum<*>>) {
        tag = iotas.toNbtList()
    }

    fun get(level: ServerLevel): List<SpellDatum<*>> = tag?.toIotaList(level) ?: mutableListOf()

    fun add(iota: SpellDatum<*>, level: ServerLevel) {
        val list = (tag?.toIotaList(level) ?: mutableListOf())
        list.add(iota)
        tag = list.toNbtList()
    }

    fun pop(level: ServerLevel): SpellDatum<*>? {
        if (size == 0)
            return null

        val list = (tag?.toIotaList(level) ?: mutableListOf())
        val iota = list.removeAt(0)
        tag = list.toNbtList()
        return iota
    }

    val size get() = tag?.size ?: 0

    fun copy(serIotaList: SerialisedIotaList) {
        tag = serIotaList.tag?.copy()
    }
}