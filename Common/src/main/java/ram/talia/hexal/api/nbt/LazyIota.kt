package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.orNull
import at.petrak.hexcasting.common.lib.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

class LazyIota(val level: ServerLevel): LazyLoad<Iota?, CompoundTag>(null) {
	override fun load(unloaded: CompoundTag) = HexIotaTypes.deserialize(unloaded, level)
	override fun unload(loaded: Iota?) = HexIotaTypes.serialize(loaded.orNull())
	override fun get(): Iota = super.get()!!
}

class LazyIotaList(val level: ServerLevel): LazyLoad<MutableList<Iota>, ListTag>(mutableListOf()) {
	override fun load(unloaded: ListTag) = unloaded.toIotaList(level)
	override fun unload(loaded: MutableList<Iota>) = loaded.toNbtList()
	override fun get(): MutableList<Iota> = super.get()!!
}