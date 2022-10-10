package ram.talia.hexal.api.everbook

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import ram.talia.hexal.api.HexalAPI
import java.util.UUID

/**
 * In a similar vein to [ram.talia.hexal.api.spell.casting.WispCastingManager], one instance of this class will be created per player; when that player leaves the world
 * a packet will be sent to their client which will save the file representation of their Everbook. When a player joins the world their Everbook will be loaded by their
 * client and sent to the server. Takes in a player's [UUID] to randomise the file content with.
 */
class Everbook(val uuid: UUID,
							 private val entries: MutableMap<String, Pair<HexPattern, CompoundTag>> = mutableMapOf(),
							 macros: List<String> = listOf()) {

	// Java apparently can't figure out the default values thing.
	constructor(uuid: UUID) : this(uuid, mutableMapOf(), listOf()) {}

	private val everbookEncrypterDecrypter = FileEncrypterDecrypter(FileEncrypterDecrypter.getKey(uuid, "AES"), "AES/CBC/PKCS5Padding")

	// private val entries: MutableMap<String, Pair<HexPattern, CompoundTag>> = mutableMapOf()

	private val macroHolder: MacroHolder = MacroHolder(this, macros)

	fun getIota(key: HexPattern, level: ServerLevel): SpellDatum<*> {
		val entry = entries[getKey(key)]
		return if (entry == null) SpellDatum.make(Widget.NULL) else SpellDatum.fromNBT(entry.second, level)
	}

	internal fun getIota(key: String) : CompoundTag {
		val entry = entries[key]
		return entry?.second ?: CompoundTag()
	}

	fun setIota(key: HexPattern, iota: SpellDatum<*>) {
		entries[getKey(key)] = Pair(key, iota.serializeToNBT())

		if (macroHolder.isMacro(key))
			macroHolder.recalcMacros()
	}

	fun setIota(key: HexPattern, iota: CompoundTag) {
		entries[getKey(key)] = Pair(key, iota)

		if (macroHolder.isMacro(key))
			macroHolder.recalcMacros()
	}

	fun removeIota(key: HexPattern) {
		entries.remove(getKey(key))

		if (macroHolder.isMacro(key))
			macroHolder.recalcMacros()
	}

	fun getMacro(key: HexPattern, level: ServerLevel) = macroHolder.getMacro(key, level)

	fun isMacro(key: HexPattern) = macroHolder.isMacro(key)

	fun toggleMacro(key: HexPattern) {
		if (macroHolder.isMacro(key))
			macroHolder.deleteMacro(key)
		else
			macroHolder.setMacro(key)
	}

	fun getKey(index: Int): HexPattern? {
		if (index >= entries.size || index < 0)
			return null

		return entries.values.map { it.first }.sortedBy { it.anglesSignature() }[index]
	}

	private fun getKey(key: HexPattern): String {
		val angles = key.anglesSignature()
		// Bookkeepers: - contains no angle characters, so can't occur any way other than this
		return angles.ifEmpty { "empty" }
	}

	fun serialiseToNBT(): CompoundTag {
		val tag = CompoundTag()
		tag.putUUID(TAG_UUID, uuid)
		tag.putList(TAG_MACROS, macroHolder.serialiseToNBT())
		entries.forEach { (key, pair) ->
			val pairCompound = CompoundTag()
			pairCompound.put(TAG_PATTERN, pair.first.serializeToNBT())
			pairCompound.put(TAG_IOTA, pair.second)
			tag.put(key, pairCompound)
		}
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
		const val TAG_MACROS = "macros"
		const val TAG_PATTERN = "pattern"
		const val TAG_IOTA = "iota"

		@JvmStatic
		fun fromNbt(tag: CompoundTag): Everbook {
			val entries: MutableMap<String, Pair<HexPattern, CompoundTag>> = mutableMapOf()

			tag.allKeys.forEach {
				if (it.equals(TAG_UUID) || it.equals(TAG_MACROS))
					return@forEach
				val pairCompound = tag.getCompound(it)
				if (pairCompound.hasCompound(TAG_PATTERN) && pairCompound.hasCompound(TAG_IOTA))
					entries[it] = Pair(HexPattern.fromNBT(pairCompound.getCompound(TAG_PATTERN)), pairCompound.getCompound(TAG_IOTA))
			}

			val macros = if (tag.hasList(TAG_MACROS)) tag.getList(TAG_MACROS, Tag.TAG_STRING).map { (it as StringTag).asString } else listOf()

			return Everbook(tag.getUUID(TAG_UUID), entries, macros)
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