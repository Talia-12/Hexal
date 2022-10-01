package ram.talia.hexal.api.everbook

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.HexalAPI
import java.util.UUID

/**
 * In a similar vein to [ram.talia.hexal.api.spell.casting.WispCastingManager], once instance of this class will be created per player; when that player leaves the world
 * a packet will be sent to their client which will save the file representation of their Everbook. When a player joins the world their Everbook will be loaded by their
 * client and sent to the server. Takes in a player's [UUID] to randomise the file content with.
 */
class Everbook(val uuid: UUID) {
	private val everbookEncrypterDecrypter = FileEncrypterDecrypter(FileEncrypterDecrypter.getKey(uuid, "AES"), "AES/CBC/PKCS5Padding")

	private val entries: MutableMap<String, CompoundTag> = mutableMapOf()

	constructor(uuid: UUID, entries: MutableMap<String, CompoundTag>) : this(uuid) {
		this.entries.putAll(entries)
	}

	fun getIota(key: HexPattern, level: ServerLevel): SpellDatum<*>? {
		val entry = entries[getKey(key)]
		return if (entry == null) SpellDatum.make(Widget.NULL) else SpellDatum.fromNBT(entry, level)
	}

	fun setIota(key: HexPattern, iota: SpellDatum<*>) {
		entries[getKey(key)] = iota.serializeToNBT()
	}

	fun setIota(key: HexPattern, iota: CompoundTag) {
		entries[getKey(key)] = iota
	}

	fun removeIota(key: HexPattern) = entries.remove(getKey(key))

	private fun getKey(key: HexPattern): String {
		val angles = key.anglesSignature()
		// Bookkeepers: - contains no angle characters, so can't occur any way other than this
		return if (angles.isEmpty()) "empty"  else angles
	}

	fun serialiseToNBT(): CompoundTag {
		val tag = CompoundTag()
		tag.putUUID(TAG_UUID, uuid)
		entries.forEach { (pattern, iota) ->  tag.put(pattern, iota) }
		return tag
	}

	/**
	 * This function saves the Everbook to the client's disk so that it is accessible between worlds (ONLY CALL ON THE CLIENT).
	 */
	fun saveToDisk() {
		// has to be here rather than an instance variable so that it doesn't try to access Minecraft on the server thread.
		val EVERBOOK_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("everbook.dat")

		val tag = this.serialiseToNBT()
		HexalAPI.LOGGER.info("saving everbook $tag for $uuid at $EVERBOOK_PATH")
		everbookEncrypterDecrypter.encrypt(tag, EVERBOOK_PATH.toFile())
	}

	companion object {
		const val TAG_UUID = "uuid"

		@JvmStatic
		fun fromNbt(tag: CompoundTag): Everbook {
			val entries: MutableMap<String, CompoundTag> = mutableMapOf()

			tag.allKeys.forEach { if (!it.equals(TAG_UUID)) entries[it] = tag.getCompound(it) }

			return Everbook(tag.getUUID(TAG_UUID), entries)
		}

		@JvmStatic
		fun fromDisk(uuid: UUID): Everbook {
			// has to be here rather than an instance variable so that it doesn't try to access Minecraft on the server thread.
			val EVERBOOK_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("everbook.dat")

			val everbookEncrypterDecrypter = FileEncrypterDecrypter(FileEncrypterDecrypter.getKey(uuid, "AES"), "AES/CBC/PKCS5Padding")
			val tag = everbookEncrypterDecrypter.decryptCompound(EVERBOOK_PATH.toFile()) ?: return Everbook(uuid)

			HexalAPI.LOGGER.info("loading everbook $tag for $uuid from $EVERBOOK_PATH")

			return fromNbt(tag)
		}
	}
}