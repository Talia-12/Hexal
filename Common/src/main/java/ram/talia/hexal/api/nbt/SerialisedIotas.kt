package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

class SerialisedIota(var tag: CompoundTag?) {
    fun set(iota: Iota) {
        tag = HexIotaTypes.serialize(iota)
    }

    fun get(level: ServerLevel): Iota? = tag?.let { HexIotaTypes.deserialize(it, level) }

    fun copy(serIota: SerialisedIota) {
        tag = serIota.tag?.copy()
    }
}

class SerialisedIotaList(var tag: ListTag?) {
    fun set(iotas: List<Iota>) {
        tag = iotas.toNbtList()
    }

    fun get(level: ServerLevel): List<Iota> = tag?.toIotaList(level) ?: mutableListOf()

    fun add(iota: Iota, level: ServerLevel) {
        val list = (tag?.toIotaList(level) ?: mutableListOf())
        list.add(iota)
        tag = list.toNbtList()
    }

    fun pop(level: ServerLevel): Iota? {
        if (size == 0)
            return null

        val list = (tag?.toIotaList(level) ?: mutableListOf())
        val iota = list.removeAt(0)
        tag = list.toNbtList()
        return iota
    }

    val size = tag?.size ?: 0

    fun copy(serIotaList: SerialisedIotaList) {
        tag = serIotaList.tag?.copy()
    }
}