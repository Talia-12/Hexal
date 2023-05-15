package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

class SerialisedIota(private var tagOrIota: Either<CompoundTag, Iota>?) {
    var tag: CompoundTag?
        get() = tagOrIota?.map({ it }, { HexIotaTypes.serialize(it) })
        set(value) { tagOrIota = Either.left(value) }

    fun set(iota: Iota) {
        tagOrIota = Either.right(iota)
    }

    fun get(level: ServerLevel): Iota? = tagOrIota?.map({ HexIotaTypes.deserialize(it, level) }, { mapIotaStillValid(it) })

    fun copy(serIota: SerialisedIota) {
        tagOrIota = serIota.tagOrIota?.mapBoth({ it.copy() }, { it })
    }

    fun refresh() {
        tagOrIota = tagOrIota?.mapRight { mapIotaStillValid(it) }
    }
}

class SerialisedIotaList(private var tagOrIotas: Either<ListTag, List<Iota>>?) {
    var tag: ListTag?
        get() = tagOrIotas?.map({ it }, { it.toNbtList() })
        set(value) { tagOrIotas = Either.left(value) }

    fun set(iotas: List<Iota>) {
        tagOrIotas = Either.right(iotas)
    }

    fun get(level: ServerLevel): List<Iota> = tagOrIotas?.map({ it.toIotaList(level) }, { mapIotaListStillValid(it) }) ?: mutableListOf()

    fun add(iota: Iota, level: ServerLevel) {
        val iotas = get(level).toMutableList()
        iotas.add(iota)
        set(iotas)
    }

    fun pop(level: ServerLevel): Iota? {
        if (size == 0)
            return null

        val iotas = get(level).toMutableList()
        val iota = iotas.removeFirst()
        set(iotas)
        return iota
    }

    val size: Int
        get() = tagOrIotas?.map({ it.size }, { it.size }) ?: 0

    fun copy(serIotaList: SerialisedIotaList) {
        tagOrIotas = serIotaList.tagOrIotas?.mapBoth({ it.copy() }, { it })
    }

    fun refresh() {
        tagOrIotas = tagOrIotas?.mapRight { mapIotaListStillValid(it) }
    }
}

/**
 * Takes an iota, and returns that iota with any [EntityIota]s whose entities no longer exist set to null.
 */
fun mapIotaStillValid(iota: Iota): Iota = when (iota) {
    is ListIota -> ListIota(mapIotaListStillValid(iota.list.toList()))
    is EntityIota -> if (iota.entity.isRemoved) NullIota() else iota
    else -> iota
}

/**
 * Takes a list of iotas, and returns that list with any [EntityIota]s whose entities no longer exist set to null.
 */
fun mapIotaListStillValid(iotas: List<Iota>): List<Iota> {
    return iotas.map { mapIotaStillValid(it) }
}