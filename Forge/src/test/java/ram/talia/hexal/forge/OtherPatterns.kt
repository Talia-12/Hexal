package ram.talia.hexal.forge

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexActions.*
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.lib.hex.HexalActions

object OtherPatterns {
	@JvmField
	val REVEAL = patternOf(PRINT)

	@JvmField
	val COMPASS = patternOf(HexAPI.modLoc("entity_pos/foot"))

	@JvmField
	val NOOP = PatternIota(HexPattern.fromAngles("", HexDir.SOUTH_EAST))
	@JvmField
	val DROP = PatternIota(HexPattern.fromAngles("a", HexDir.SOUTH_EAST))
	@JvmField
	val SWAP = patternOf(HexAPI.modLoc("swap"))

	@JvmField
	val EQUALITY = patternOf(HexAPI.modLoc("equals"))
	@JvmField
	val INEQUALITY = patternOf(HexAPI.modLoc("not_equals"))
	@JvmField
	val AUGERS = patternOf(IF)

	@JvmField
	val NULLARY = patternOf(HexAPI.modLoc("const/null"))

	@JvmField
	val ZERO = PatternIota(HexPattern.fromAngles("aqaa", HexDir.EAST))
	@JvmField
	val ONE = PatternIota(HexPattern.fromAngles("aqaaw", HexDir.EAST))
	@JvmField
	val FOUR = PatternIota(HexPattern.fromAngles("aqaawaa", HexDir.EAST))

	@JvmField
	val GEMINIS_DISINTEGRATION = patternOf(HexAPI.modLoc("duplicate"))
	@JvmField
	val FLOCKS_DISINTEGRATION = patternOf(SPLAT)

	@JvmField
	val SELECTION_DISTILLATION = patternOf(INDEX)

	@JvmField
	val HERMES = patternOf(EVAL)

	@JvmField
	val INTRO = PatternIota(HexPattern.fromAngles("qqq", HexDir.WEST))
	@JvmField
	val RETRO = PatternIota(HexPattern.fromAngles("eee", HexDir.EAST))



	@JvmField
	val TIMEKEEPER: PatternIota = PatternIota(HexalActions.CURRENT_TICK.prototype)

	@JvmField
	val ZONE_DSTL_WISP: PatternIota = PatternIota(HexalActions.ZONE_ENTITY_WISP.prototype)
	@JvmField
	val WISP_TRIGGER_COMM: PatternIota = PatternIota(HexalActions.WISP_TRIGGER_COMM.prototype)

	@JvmField
	val LINK: PatternIota = PatternIota(HexalActions.LINK.prototype)
	@JvmField
	val POPULARITY: PatternIota = PatternIota(HexalActions.LINK_NUM.prototype)

	@JvmField
	val SEND_IOTA: PatternIota = PatternIota(HexalActions.LINK_COMM_SEND.prototype)
	@JvmField
	val RECITATION: PatternIota = PatternIota(HexalActions.LINK_COMM_READ.prototype)

	private fun patternOf(op: ActionRegistryEntry): PatternIota = PatternIota(op.prototype)
	private fun patternOf(loc: ResourceLocation): PatternIota = PatternIota(REGISTRY.get(loc)!!.prototype)
}