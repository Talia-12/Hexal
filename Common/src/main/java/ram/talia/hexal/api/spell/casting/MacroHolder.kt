package ram.talia.hexal.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.spellListOf
import at.petrak.hexcasting.api.utils.hasCompound
import at.petrak.hexcasting.api.utils.serializeToNBT
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.everbook.Everbook

/**
 * Attached to a player, and stores all the macros that player has created.
 */
class MacroHolder() {
	private val entries: MutableMap<String, ListTag> = mutableMapOf()

	constructor(entries: MutableMap<String, ListTag>) : this() {
		this.entries.putAll(entries)
	}

	fun getIotas(key: HexPattern, level: ServerLevel): SpellList {
		val entry = entries[getKey(key)]
		return if (entry == null) SpellList.fromNBT(ListTag(), level) else SpellList.fromNBT(entry, level) // JANK
	}

	fun setIotas(key: HexPattern, iotas: SpellList) {
		entries[getKey(key)] = iotas.serializeToNBT()
	}

	fun setIotas(key: HexPattern, iotas: ListTag) {
		entries[getKey(key)] = iotas
	}

	private fun getKey(key: HexPattern): String {
		val angles = key.anglesSignature()
		// Bookkeepers: - contains no angle characters, so can't occur any way other than this
		return if (angles.isEmpty()) "empty" else angles
	}

	fun serialiseToNBT(): CompoundTag {
		val tag = CompoundTag()
		entries.forEach { (key, iotas) -> tag.put(key, iotas) }
		return tag
	}

	companion object {
		@JvmStatic
		fun fromNbt(tag: CompoundTag): MacroHolder {
			val entries: MutableMap<String, ListTag> = mutableMapOf()

			tag.allKeys.forEach { entries[it] = tag.get(it) as ListTag }

			return MacroHolder(entries)
		}
	}
}