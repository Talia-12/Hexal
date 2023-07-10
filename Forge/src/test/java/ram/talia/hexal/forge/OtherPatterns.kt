package ram.talia.hexal.forge

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.PatternIota
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.common.casting.operators.eval.OpEval
import at.petrak.hexcasting.common.casting.operators.lists.OpIndex
import at.petrak.hexcasting.common.casting.operators.lists.OpSplat
import at.petrak.hexcasting.common.casting.operators.math.logic.OpBoolIf
import at.petrak.hexcasting.common.casting.operators.spells.OpPrint
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.lib.hex.HexalActions

object OtherPatterns {
	@JvmField
	val REVEAL = patternOf(OpPrint)

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
	val AUGERS = patternOf(OpBoolIf)

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
	val FLOCKS_DISINTEGRATION = patternOf(OpSplat)

	@JvmField
	val SELECTION_DISTILLATION = patternOf(OpIndex)

	@JvmField
	val HERMES = patternOf(OpEval)

	@JvmField
	val INTRO = PatternIota(HexPattern.fromAngles("qqq", HexDir.WEST))
	@JvmField
	val RETRO = PatternIota(HexPattern.fromAngles("eee", HexDir.EAST))



	@JvmField
	val TIMEKEEPER = HexalActions.CURRENT_TICK

	@JvmField
	val ZONE_DSTL_WISP = HexalActions.ZONE_ENTITY_WISP
	@JvmField
	val WISP_TRIGGER_COMM = HexalActions.WISP_TRIGGER_COMM

	@JvmField
	val LINK = HexalActions.LINK
	@JvmField
	val POPULARITY = HexalActions.LINK_NUM

	@JvmField
	val SEND_IOTA = HexalActions.LINK_COMM_SEND
	@JvmField
	val RECITATION = HexalActions.LINK_COMM_READ

	private fun patternOf(op: Action): PatternIota = PatternIota(PatternRegistry.lookupPattern(PatternRegistry.lookupPattern(op)!!).prototype)
	private fun patternOf(loc: ResourceLocation): PatternIota = PatternIota(PatternRegistry.lookupPattern(loc).prototype)
}