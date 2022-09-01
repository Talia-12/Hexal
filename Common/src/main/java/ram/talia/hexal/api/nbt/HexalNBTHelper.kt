package ram.talia.hexal.api.spell

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asList
import at.petrak.hexcasting.api.utils.asUUID
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.syncher.EntityDataSerializer
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

fun ListTag.toUUIDList(): MutableList<UUID> {
	val out = mutableListOf<UUID>()

	for (uuidTag in this) {
		out.add(uuidTag.asUUID)
	}

	return out
}

fun List<UUID>.toNbtList(): ListTag {
	val listTag = ListTag()

	this.forEach { uuid -> listTag.add(NbtUtils.createUUID(uuid)) }

	return listTag
}