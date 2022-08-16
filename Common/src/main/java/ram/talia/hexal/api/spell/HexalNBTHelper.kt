package ram.talia.hexal.api.spell

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.asCompound
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel

fun ListTag.toIotaList(level: ServerLevel): MutableList<SpellDatum<*>> {
	val out = ArrayList<SpellDatum<*>>()
	for (patTag in this) {
		val tag = patTag.asCompound
		if (tag.size() != 1) {
			out.add(SpellDatum.make(HexPattern.fromNBT(tag)))
		} else {
			out.add(SpellDatum.fromNBT(tag, level))
		}
	}
	return out
}

fun List<SpellDatum<*>>.toNbtList(): ListTag {
	val patsTag = ListTag()
	for (pat in this) {
		patsTag.add(pat.serializeToNBT())
	}
	return patsTag
}
