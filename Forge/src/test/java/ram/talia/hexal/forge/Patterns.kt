package ram.talia.hexal.forge

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.common.casting.operators.OpEntityPos
import at.petrak.hexcasting.common.casting.operators.eval.OpEval
import at.petrak.hexcasting.common.casting.operators.lists.OpIndex
import at.petrak.hexcasting.common.casting.operators.lists.OpSplat
import at.petrak.hexcasting.common.casting.operators.math.logic.OpBoolAnd
import at.petrak.hexcasting.common.casting.operators.math.logic.OpBoolIdentityKindOf
import at.petrak.hexcasting.common.casting.operators.math.logic.OpBoolOr
import at.petrak.hexcasting.common.casting.operators.math.logic.OpEquality
import at.petrak.hexcasting.common.casting.operators.spells.OpPrint
import at.petrak.hexcasting.common.casting.operators.stack.OpDuplicate
import at.petrak.hexcasting.common.casting.operators.stack.OpSwap
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.casting.actions.OpCurrentTick
import ram.talia.hexal.common.casting.actions.spells.link.OpLinkEntity
import ram.talia.hexal.common.casting.actions.spells.link.OpNumLinked
import ram.talia.hexal.common.casting.actions.spells.link.OpReadReceivedIota
import ram.talia.hexal.common.casting.actions.spells.link.OpSendIota

object Patterns {
	@JvmField
	val REVEAL = patternOf(OpPrint)

	@JvmField
	val COMPASS = patternOf(OpEntityPos)

	@JvmField
	val DROP = SpellDatum.make(HexPattern.fromAngles("a", HexDir.SOUTH_EAST))
	@JvmField
	val SWAP = patternOf(OpSwap)

	@JvmField
	val EQUALITY = patternOf(HexAPI.modLoc("equals"))
	@JvmField
	val IDENTITY = patternOf(OpBoolIdentityKindOf)
	@JvmField
	val CONJUNCTION = patternOf(OpBoolAnd)
	@JvmField
	val DISJUNCTION = patternOf(OpBoolOr)

	@JvmField
	val NULLARY = patternOf(HexAPI.modLoc("const/null"))

	@JvmField
	val ZERO = SpellDatum.make(HexPattern.fromAngles("aqaa", HexDir.EAST))
	@JvmField
	val ONE = SpellDatum.make(HexPattern.fromAngles("aqaaw", HexDir.EAST))
	@JvmField
	val FOUR = SpellDatum.make(HexPattern.fromAngles("aqaawaa", HexDir.EAST))

	@JvmField
	val GEMINIS_DISINTEGRATION = patternOf(OpDuplicate)
	@JvmField
	val FLOCKS_DISINTEGRATION = patternOf(OpSplat)

	@JvmField
	val SELECTION_DISTILLATION = patternOf(OpIndex)

	@JvmField
	val HERMES = patternOf(OpEval)

	@JvmField
	val INTRO = SpellDatum.make(HexPattern.fromAngles("qqq", HexDir.WEST))
	@JvmField
	val RETRO = SpellDatum.make(HexPattern.fromAngles("eee", HexDir.EAST))



	@JvmField
	val TIMEKEEPER = patternOf(OpCurrentTick)

	@JvmField
	val ZONE_DSTL_WISP = patternOf(HexalAPI.modLoc("zone_entity/wisp"))

	@JvmField
	val LINK_ENTITY = patternOf(OpLinkEntity)
	@JvmField
	val POPULARITY = patternOf(OpNumLinked)

	@JvmField
	val SEND_IOTA = patternOf(OpSendIota)
	@JvmField
	val RECITATION = patternOf(OpReadReceivedIota)

	private fun patternOf(op: Operator): SpellDatum<*> = SpellDatum.make(PatternRegistry.lookupPattern(PatternRegistry.lookupPattern(op)!!).prototype)
	private fun patternOf(loc: ResourceLocation): SpellDatum<*> = SpellDatum.make(PatternRegistry.lookupPattern(loc).prototype)
}