@file:Suppress("unused")

package ram.talia.hexal.common.casting

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.common.casting.operators.selectors.*
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerTypes
import ram.talia.hexal.common.casting.actions.*
import ram.talia.hexal.common.casting.actions.everbook.*
import ram.talia.hexal.common.casting.actions.spells.*
import ram.talia.hexal.common.casting.actions.spells.great.*
import ram.talia.hexal.common.casting.actions.spells.link.*
import ram.talia.hexal.common.casting.actions.spells.wisp.*
import ram.talia.hexal.common.entities.BaseWisp

object Patterns {

	@JvmField
	var PATTERNS: MutableList<Triple<HexPattern, ResourceLocation, Action>> = ArrayList()
	@JvmField
	var PER_WORLD_PATTERNS: MutableList<Triple<HexPattern, ResourceLocation, Action>> = ArrayList()

	// ============================ Type Comparison ===================================
	@JvmField
	val TYPE_BLOCK = make(HexPattern.fromAngles("qaqqaea", HexDir.EAST), modLoc("type/block"), OpTypeBlock)
	@JvmField
	val TYPE_ENTITY = make(HexPattern.fromAngles("qawde", HexDir.SOUTH_WEST), modLoc("type/entity"), OpTypeEntity)
	@JvmField
	val TYPE_IOTA = make(HexPattern.fromAngles("awd", HexDir.SOUTH_WEST), modLoc("type/iota"), OpTypeIota)

	// ========================== Misc Info Gathering =================================
	@JvmField
	val CURRENT_TICK = make(HexPattern.fromAngles("ddwaa", HexDir.NORTH_WEST), modLoc("current_tick"), OpCurrentTick)
	@JvmField
	val REMAINING_EVALS = make(HexPattern.fromAngles("qqaed", HexDir.SOUTH_EAST), modLoc("remaining_evals"), OpRemainingEvals)
	@JvmField
	val BREATH = make(HexPattern.fromAngles("aqawdwaqawd", HexDir.NORTH_WEST), modLoc("breath"), OpGetBreath)
	@JvmField
	val HEALTH =make(HexPattern.fromAngles("aqwawqa", HexDir.NORTH_WEST), modLoc("health"), OpGetHealth)
	@JvmField
	val LIGHT_LEVEL = make(HexPattern.fromAngles("qedqde", HexDir.NORTH_EAST), modLoc("light_level"), OpGetLightLevel)

	// =============================== Misc Maths =====================================
	@JvmField
	val FACTORIAL = make(HexPattern.fromAngles("wawdedwaw", HexDir.SOUTH_EAST), modLoc("factorial"), OpFactorial)
	@JvmField
	val RUNNING_SUM = make(HexPattern.fromAngles("aea", HexDir.WEST), modLoc("running/sum"), OpRunningOp (0.0)
	{ running, iota ->
		running + ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double"))
	})
	@JvmField
	val RUNNING_MUL = make(HexPattern.fromAngles("qaawaaq", HexDir.NORTH_EAST), modLoc("running/mul"), OpRunningOp (1.0)
	{ running, iota ->
		running * ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double"))
	})

	// ================================ Everbook ======================================
	@JvmField
	val EVERBOOK_READ = make(HexPattern.fromAngles("eweeewedqdeddw", HexDir.NORTH_EAST), modLoc("everbook/read"), OpEverbookRead)
	@JvmField
	val EVERBOOK_WRITE = make(HexPattern.fromAngles("qwqqqwqaeaqaaw", HexDir.SOUTH_EAST), modLoc("everbook/write"), OpEverbookWrite)
	@JvmField
	val EVERBOOK_DELETE = make(HexPattern.fromAngles("qwqqqwqaww", HexDir.SOUTH_EAST), modLoc("everbook/delete"), OpEverbookDelete)
	@JvmField
	val EVERBOOK_TOGGLE_MACRO = make(HexPattern.fromAngles("eweeewedww", HexDir.SOUTH_WEST), modLoc("everbook/toggle_macro"), OpToggleMacro)

	// ============================== Misc Spells =====================================
	@JvmField
	val SMELT = make(HexPattern.fromAngles("wqqqwqqadad", HexDir.EAST), modLoc("smelt"), OpSmelt)
	@JvmField
	val FREEZE = make(HexPattern.fromAngles("weeeweedada", HexDir.WEST), modLoc("freeze"), OpFreeze)
	@JvmField
	val FALLING_BLOCK = make(HexPattern.fromAngles("wqwawqwqwqwqwqw", HexDir.EAST), modLoc("falling_block"), OpFallingBlock)
	@JvmField
	val PLACE_TYPE = make(HexPattern.fromAngles("eeeeedeeeee", HexDir.WEST), modLoc("place_type"), OpPlaceType)

	// =============================== Wisp Stuff =====================================
	@JvmField
	val WISP_SUMMON_PROJECTILE = make(HexPattern.fromAngles("aqaeqeeeee", HexDir.NORTH_WEST), modLoc("wisp/summon/projectile"), OpSummonWisp(false))
	@JvmField
	val WISP_SUMMON_TICKING = make(HexPattern.fromAngles("aqaweewaqawee", HexDir.NORTH_WEST), modLoc("wisp/summon/ticking"), OpSummonWisp(true))
	@JvmField
	val WISP_MEDIA = make(HexPattern.fromAngles("aqaweewaqaweedw", HexDir.NORTH_WEST), modLoc("wisp/media"), OpWispMedia)
	@JvmField
	val WISP_HEX = make(HexPattern.fromAngles("aweewaqaweewaawww", HexDir.SOUTH_EAST), modLoc("wisp/hex"), OpWispHex)
	@JvmField
	val WISP_OWNER = make(HexPattern.fromAngles("dwqqwdedwqqwddwww", HexDir.SOUTH_WEST), modLoc("wisp/owner"), OpWispOwner)

	// Set and Get Move Target WEST awqwawqaw
	@JvmField
	val WISP_MOVE_TARGET_SET = make(HexPattern.fromAngles("awqwawqaw", HexDir.WEST), modLoc("wisp/move/target/set"), OpMoveTargetSet)
	@JvmField
	val WISP_MOVE_TARGET_GET = make(HexPattern.fromAngles("ewdwewdew", HexDir.EAST), modLoc("wisp/move/target/get"), OpMoveTargetGet)
	@JvmField
	val WISP_MOVE_SPEED_SET = make(HexPattern.fromAngles("aeawqqqae", HexDir.WEST), modLoc("wisp/move/speed/set"), OpMoveSpeedSet)
	@JvmField
	val WISP_MOVE_SPEED_GET = make(HexPattern.fromAngles("eeewdqdee", HexDir.EAST), modLoc("wisp/move/speed/get"), OpMoveSpeedGet)


	// Entity purification and Zone distillations
	@JvmField
	val GET_ENTITY_WISP = make(HexPattern.fromAngles("qqwdedwqqdaqaaww", HexDir.SOUTH_EAST),
		                         modLoc("get_entity/wisp"),
		                         OpGetEntityAt{ entity -> entity is BaseWisp })
	@JvmField
	val ZONE_ENTITY_WISP = make(HexPattern.fromAngles("qqwdedwqqwdeddww", HexDir.SOUTH_EAST),
		                          modLoc("zone_entity/wisp"),
		                          OpGetEntitiesBy({ entity -> entity is BaseWisp }, false))
	@JvmField
	val ZONE_ENTITY_NOT_WISP = make(HexPattern.fromAngles("eewaqaweewaqaaww", HexDir.NORTH_EAST),
		                              modLoc("zone_entity/not_wisp"),
		                              OpGetEntitiesBy({ entity -> entity is BaseWisp }, true))

	// Triggers
	@JvmField
	val WISP_TRIGGER_TICK = make(HexPattern.fromAngles("aqawded", HexDir.NORTH_WEST),
		                           modLoc("wisp/trigger/tick"),
		                           OpWispSetTrigger(WispTriggerTypes.TICK_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_COMM = make(HexPattern.fromAngles("aqqqqqwdeddw", HexDir.EAST),
		                           modLoc("wisp/trigger/comm"),
		                           OpWispSetTrigger(WispTriggerTypes.COMM_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_MOVE = make(HexPattern.fromAngles("eqwawqwaqww", HexDir.EAST),
		                           modLoc("wisp/trigger/move"),
		                           OpWispSetTrigger(WispTriggerTypes.MOVE_TRIGGER_TYPE))

	val WISP_SEON_GET = make(HexPattern.fromAngles("daqweewqaeaqweewqaqwwww", HexDir.EAST),
			modLoc("wisp/seon/get"),
			OpSeonWispGet)

	// Great
	@JvmField
	val CONSUME_WISP = make(HexPattern.fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
						              modLoc("wisp/consume"),
						              OpConsumeWisp,
						              true)
	val WISP_SEON_SET = make(HexPattern.fromAngles("aqweewqaeaqweewqaqwww", HexDir.SOUTH_WEST),
			                 modLoc("wisp/seon/set"),
	                         OpSeonWispSet,
			        true)

	// =============================== Link Stuff =====================================
	@JvmField
	val LINK_ENTITY = make(HexPattern.fromAngles("eaqaaeqqqqqaweaqaaw", HexDir.EAST), modLoc("link/link_entity"), OpLinkEntity)
	@JvmField
	val LINK_ENTITIES = make(HexPattern.fromAngles("eqqqqqawqeeeeedww", HexDir.EAST), modLoc("link/link_two_entities"), OpLinkEntities)
	@JvmField
	val UNLINK = make(HexPattern.fromAngles("qdeddqeeeeedwqdeddw", HexDir.WEST), modLoc("link/unlink"), OpUnlink)
	@JvmField
	val LINK_GET = make(HexPattern.fromAngles("eqqqqqaww", HexDir.EAST), modLoc("link/get"), OpGetLinked)
	@JvmField
	val LINK_GET_INDEX = make(HexPattern.fromAngles("aeqqqqqawwd", HexDir.SOUTH_WEST), modLoc("link/get_index"), OpGetLinkedIndex)
	@JvmField
	val LINK_NUM = make(HexPattern.fromAngles("qeeeeedww", HexDir.WEST), modLoc("link/num"), OpNumLinked)
	@JvmField
	val LINK_COMM_SEND = make(HexPattern.fromAngles("qqqqqwdeddw", HexDir.NORTH_WEST), modLoc("link/comm/send"), OpSendIota)
	@JvmField
	val LINK_COMM_READ = make(HexPattern.fromAngles("weeeeew", HexDir.NORTH_EAST), modLoc("link/comm/read"), OpReadReceivedIota)
	@JvmField
	val LINK_COMM_NUM = make(HexPattern.fromAngles("aweeeeewaa", HexDir.SOUTH_EAST), modLoc("link/comm/num"), OpNumReceivedIota)
	@JvmField
	val LINK_COMM_OPEN_TRANSMIT = make(HexPattern.fromAngles("qwdedwq", HexDir.WEST), modLoc("link/comm/open_transmit"), OpOpenTransmit)
	@JvmField
	val LINK_COMM_CLOSE_TRANSMIT = make(HexPattern.fromAngles("ewaqawe", HexDir.EAST), modLoc("link/comm/close_transmit"), OpCloseTransmit)

	private fun make (pattern: HexPattern, location: ResourceLocation, operator: Action, isPerWorld: Boolean = false): Triple<HexPattern, ResourceLocation, Action> {
		val triple = Triple(pattern, location, operator)
		if (isPerWorld)
			PER_WORLD_PATTERNS.add(triple)
		else
			PATTERNS.add(triple)
		return triple
	}
}