package ram.talia.hexal.api.everbook

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.toCompoundTagList

/**
 * One instance per Everbook, holds compiled versions of all the macros that a player has created (by marking patterns in the Everbook as keys for macros).
 */
class MacroHolder(val everbook: Everbook) {
	private val macros: MutableMap<String, List<CompoundTag>?> = mutableMapOf()

	constructor(everbook: Everbook, macros: MutableMap<String, List<CompoundTag>?>) : this(everbook) {
		this.macros.putAll(macros)
	}

	fun getMacro(key: HexPattern): List<CompoundTag>? {
		return macros[getKey(key)]
	}

	fun isMacro(key: HexPattern): Boolean {
		return macros.containsKey(getKey(key))
	}

	fun setMacro(key: HexPattern) {
		macros[getKey(key)] = listOf()
		recalcMacros()
	}

	fun deleteMacro(key: HexPattern) {
		macros.remove(getKey(key))
		recalcMacros()
	}

	/**
	 * Called every time the [Everbook] changes in a way that would affect the macros, or a macro is added/deleted.
	 */
	fun recalcMacros() {
		// stores which keys haven't been resolved yet, meaning they still need to get data from the Everbook.
		val unresolvedKeys: MutableList<String> = mutableListOf(*macros.keys.toTypedArray())

		// stores which keys have been relevant in the resolution of the current key, used to stop infinite loops.
		val currentKeys: MutableList<String> = mutableListOf()

		while (unresolvedKeys.isNotEmpty()) {
			currentKeys.clear()
			currentKeys.add(unresolvedKeys[0])
			unresolvedKeys.removeAt(0)

			resolveKeys(unresolvedKeys, currentKeys)
		}
	}

	/**
	 * Takes in [unresolvedKeys], the list of keys for macros that haven't been resolved down to a list of non-macro iotas yet, as well as [currentKeys], the stack of keys
	 * that are currently being resolved. Resolves the key that is on top of [currentKeys], by getting the list of iotas at that key from the [Everbook] and for each one,
	 * if it's another macro, fetching the list for that macro or resolving it if it's unresolved. This is a recursive method.
	 */
	private fun resolveKeys(unresolvedKeys: MutableList<String>, currentKeys: MutableList<String>) {
		val key = currentKeys[currentKeys.size - 1]

		var iotas = extractList(everbook.getIota(key)) ?: return let {
			currentKeys.remove(key)
			macros[key] = null
		}

		// contains the index that needs to be spliced at, and the thing to be spliced in there
		val toSpliceIn: MutableList<Pair<Int, MutableList<CompoundTag>>> = mutableListOf()

		for (i in 0 until iotas.size) {
			val pattern = parseToPattern(iotas[i]) ?: continue
			val possibleKey = getKey(pattern)

			if (macros.keys.contains(possibleKey)) {
				if (currentKeys.contains(possibleKey)) {
					HexalAPI.LOGGER.info("key $possibleKey inside iota list for $key resulted in infinite recursion.")
					currentKeys.remove(key)
					macros[key] = null
					return
				}
				if (unresolvedKeys.contains(possibleKey)) {
					currentKeys.add(possibleKey)
					unresolvedKeys.remove(possibleKey)
					resolveKeys(unresolvedKeys, currentKeys)
				}

				val macroResult = macros[possibleKey]?.toMutableList()
				if (macroResult != null)
					toSpliceIn += Pair(i, macroResult)
			}
		}

		// loop in reverse so that the splicing doesn't affect the next indices to splice.
		for (i in (toSpliceIn.size-1) downTo 0) {
			val index = toSpliceIn[i].first
			val iotasBefore = iotas.slice(0 until index)
			val iotasAfter = iotas.slice((index+1) until iotas.size)

			iotas = (iotasBefore + toSpliceIn[i].second + iotasAfter).toMutableList()
		}

		// remove the key we're currently resolving from currentKeys at the end so that recursion checking works for self-repeating recursion.
		currentKeys.remove(key)
		macros[key] = iotas
	}

	private fun extractList(spellListTag: CompoundTag): MutableList<CompoundTag>? {
		val keys = spellListTag.allKeys

		if (keys.size != 1)
			return null
		return when (val key = keys.iterator().next()) {
			SpellDatum.TAG_LIST -> spellListTag.getList(key, Tag.TAG_COMPOUND.toInt()).toCompoundTagList()
			else -> null
		}
	}

	private fun parseToPattern(patternTag: CompoundTag): HexPattern? {
		val keys = patternTag.allKeys

		if (keys.size != 1)
			return null
		return when (val key = keys.iterator().next()) {
			SpellDatum.TAG_PATTERN -> HexPattern.fromNBT(patternTag.getCompound(SpellDatum.TAG_PATTERN))
			else -> null
		}
	}

	private fun getKey(key: HexPattern): String {
		val angles = key.anglesSignature()
		// Bookkeepers: - contains no angle characters, so can't occur any way other than this
		return angles.ifEmpty { "empty" }
	}
}