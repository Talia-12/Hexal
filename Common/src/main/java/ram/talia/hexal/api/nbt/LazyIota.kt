package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.spell.toIotaList
import ram.talia.hexal.api.spell.toNbtList

class LazyIota(val level: ServerLevel): LazyLoad<SpellDatum<*>, CompoundTag>(Either.left(SpellDatum.make(Widget.NULL))) {
	override fun load(unloaded: CompoundTag) = SpellDatum.Companion.fromNBT(unloaded, level)
	override fun unload(loaded: SpellDatum<*>) = loaded.serializeToNBT()
	override fun get(): SpellDatum<*> = either.map({ it }, { load(it) })
}

class LazyIotaList(val level: ServerLevel): LazyLoad<MutableList<SpellDatum<*>>, ListTag>(Either.left(mutableListOf())) {
	override fun load(unloaded: ListTag) = unloaded.toIotaList(level)
	override fun unload(loaded: MutableList<SpellDatum<*>>) = loaded.toNbtList()
	override fun get(): MutableList<SpellDatum<*>> = either.map({ it }, { load(it) })
}