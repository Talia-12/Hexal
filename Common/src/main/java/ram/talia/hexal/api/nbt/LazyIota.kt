package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

class LazyIota(val level: ServerLevel): LazyLoad<SpellDatum<*>, CompoundTag>(SpellDatum.make(Widget.NULL)) {
	override fun load(unloaded: CompoundTag) = SpellDatum.Companion.fromNBT(unloaded, level)
	override fun unload(loaded: SpellDatum<*>) = loaded.serializeToNBT()
	override fun get(): SpellDatum<*> = super.get()!!
}

class LazyIotaList(val level: ServerLevel): LazyLoad<MutableList<SpellDatum<*>>, ListTag>(mutableListOf()) {
	override fun load(unloaded: ListTag) = unloaded.toIotaList(level)
	override fun unload(loaded: MutableList<SpellDatum<*>>) = loaded.toNbtList()
	override fun get(): MutableList<SpellDatum<*>> = super.get()!!
}