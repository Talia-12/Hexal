package ram.talia.hexal.api.spell

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asInt
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import java.util.*

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

@JvmName("toNbtListSpellDatum")
fun List<SpellDatum<*>>.toNbtList(): ListTag {
	val patsTag = ListTag()
	for (pat in this) {
		patsTag.add(pat.serializeToNBT())
	}
	return patsTag
}

inline fun <reified T : Entity> ListTag.toEntityList(level: ServerLevel): MutableList<T> {
	val out = ArrayList<T>()

	for (eTag in this) {
		val uuid = NbtUtils.loadUUID(eTag)
		val entity = level.getEntity(uuid)

		if (entity != null && entity.isAlive && entity is T)
			out.add(entity)
	}

	return out
}

@JvmName("toNbtListEntity")
fun List<Entity>.toNbtList(): ListTag {
	val listTag = ListTag()

	for (entity in this) {
		listTag.add(NbtUtils.createUUID(entity.uuid))
	}

	return listTag
}

fun ListTag.toIntList(): MutableList<Int> {
	val out = mutableListOf<Int>()

	for (idTag in this) {
		out.add(idTag.asInt)
	}

	return out
}

@JvmName("toNbtListInt")

fun List<Int>.toNbtList(): ListTag {
	val listTag = ListTag()

	this.forEach { listTag.add(IntTag.valueOf(it)) }

	return listTag
}

@JvmName("toNbtListTag")
fun List<Tag>.toNbtList(): ListTag {
	val listTag = ListTag()

	this.forEach { listTag.add(it) }

	return listTag
}