@file:Suppress("unused")

package ram.talia.hexal.common.casting

import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.PatternIota
import at.petrak.hexcasting.api.spell.iota.Vec3Iota
import at.petrak.hexcasting.api.spell.math.HexDir.*
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.math.HexPattern.Companion.fromAngles
import at.petrak.hexcasting.common.casting.operators.selectors.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerTypes
import ram.talia.hexal.common.casting.actions.*
import ram.talia.hexal.common.casting.actions.everbook.*
import ram.talia.hexal.common.casting.actions.spells.*
import ram.talia.hexal.common.casting.actions.spells.gates.*
import ram.talia.hexal.common.casting.actions.spells.great.*
import ram.talia.hexal.common.casting.actions.spells.motes.*
import ram.talia.hexal.common.casting.actions.spells.link.*
import ram.talia.hexal.common.casting.actions.spells.wisp.*
import ram.talia.hexal.common.entities.BaseWisp

object Patterns {

	@JvmField
	var PATTERNS: MutableList<Triple<HexPattern, ResourceLocation, Action>> = ArrayList()
	@JvmField
	var PER_WORLD_PATTERNS: MutableList<Triple<HexPattern, ResourceLocation, Action>> = ArrayList()
	@JvmField
	val SPECIAL_HANDLERS: MutableList<Pair<ResourceLocation, PatternRegistry.SpecialHandler>> = ArrayList()

	@JvmStatic
	fun registerPatterns() {
		try {
			for ((pattern, location, action) in PATTERNS)
				PatternRegistry.mapPattern(pattern, location, action)
			for ((pattern, location, action) in PER_WORLD_PATTERNS)
				PatternRegistry.mapPattern(pattern, location, action, true)
			for ((location, handler) in SPECIAL_HANDLERS)
				PatternRegistry.addSpecialHandler(location, handler)
		} catch (e: PatternRegistry.RegisterPatternException) {
			e.printStackTrace()
		}
	}

	// ============================ Type Comparison ===================================
	@JvmField
	val TYPE_BLOCK_ITEM = make(fromAngles("qaqqaea", EAST), modLoc("type/block_item"), OpTypeBlockItem)
	@JvmField
	val TYPE_ENTITY = make(fromAngles("qawde", SOUTH_WEST), modLoc("type/entity"), OpTypeEntity)
	@JvmField
	val TYPE_IOTA = make(fromAngles("awd", SOUTH_WEST), modLoc("type/iota"), OpTypeIota)
	@JvmField
	val TYPE_ITEM_HELD = make(fromAngles("edeedqd", SOUTH_WEST), modLoc("type/item_held"), OpTypeItemHeld)

	@JvmField
	val RAYCAST_ENTITY_DYN = make(fromAngles("weaqaaw", EAST), modLoc("raycast/entity/type"), OpEntityRaycastDyn)
	@JvmField
	val GET_ENTITY_TYPE = make(fromAngles("dadqqqqqdad", NORTH_EAST), modLoc("get_entity/type"), OpGetEntityAtDyn)
	@JvmField
	val ZONE_ENTITY_TYPE = make(fromAngles("waweeeeewaw", SOUTH_EAST), modLoc("zone_entity/type"), OpGetEntitiesByDyn(false))
	@JvmField
	val ZONE_ENTITY_NOT_TYPE = make(fromAngles("wdwqqqqqwdw", NORTH_EAST), modLoc("zone_entity/not_type"), OpGetEntitiesByDyn(true))

	// ========================== Misc Info Gathering =================================
	@JvmField
	val CURRENT_TICK = make(fromAngles("ddwaa", NORTH_WEST), modLoc("current_tick"), OpCurrentTick)
	@JvmField
	val REMAINING_EVALS = make(fromAngles("qqaed", SOUTH_EAST), modLoc("remaining_evals"), OpRemainingEvals)
	@JvmField
	val BREATH = make(fromAngles("aqawdwaqawd", NORTH_WEST), modLoc("breath"), OpGetBreath)
	@JvmField
	val HEALTH = make(fromAngles("aqwawqa", NORTH_WEST), modLoc("health"), OpGetHealth)
	@JvmField
	val ARMOUR = make(fromAngles("wqqqqw", NORTH_WEST), modLoc("armour"), OpGetArmour)
	@JvmField
	val TOUGHNESS = make(fromAngles("aeqqqqea", EAST), modLoc("toughness"), OpGetToughness)
	@JvmField
	val LIGHT_LEVEL = make(fromAngles("qedqde", NORTH_EAST), modLoc("light_level"), OpGetLightLevel)

	// =============================== Misc Maths =====================================
	@JvmField
	val FACTORIAL = make(fromAngles("wawdedwaw", SOUTH_EAST), modLoc("factorial"), OpFactorial)
	@JvmField
	val RUNNING_SUM = make(fromAngles("aea", WEST), modLoc("running/sum"), OpRunningOp ({ if (it is Vec3Iota) Vec3Iota(Vec3.ZERO) else DoubleIota(0.0) })
	{ running, iota ->
		when (running) {
			is DoubleIota -> DoubleIota(running.double + ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
			is Vec3Iota -> Vec3Iota(running.vec3 + ((iota as? Vec3Iota)?.vec3 ?: throw OpRunningOp.InvalidIotaException("list.vec")))
			else -> throw OpRunningOp.InvalidIotaException("list.doublevec")
		}
	})
	@JvmField
	val RUNNING_MUL = make(fromAngles("qaawaaq", NORTH_EAST), modLoc("running/mul"), OpRunningOp ({ DoubleIota(1.0) })
	{ running, iota ->
		if (running !is DoubleIota)
			throw OpRunningOp.InvalidIotaException("list.double")
		DoubleIota(running.double * ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
	})

	// ================================ Everbook ======================================
	@JvmField
	val EVERBOOK_READ = make(fromAngles("eweeewedqdeddw", NORTH_EAST), modLoc("everbook/read"), OpEverbookRead)
	@JvmField
	val EVERBOOK_WRITE = make(fromAngles("qwqqqwqaeaqaaw", SOUTH_EAST), modLoc("everbook/write"), OpEverbookWrite)
	@JvmField
	val EVERBOOK_DELETE = make(fromAngles("qwqqqwqaww", SOUTH_EAST), modLoc("everbook/delete"), OpEverbookDelete)
	@JvmField
	val EVERBOOK_TOGGLE_MACRO = make(fromAngles("eweeewedww", SOUTH_WEST), modLoc("everbook/toggle_macro"), OpToggleMacro)

	// ============================== Misc Spells =====================================
	@JvmField
	val SMELT = make(fromAngles("wqqqwqqadad", EAST), modLoc("smelt"), OpSmelt)
	@JvmField
	val FREEZE = make(fromAngles("weeeweedada", WEST), modLoc("freeze"), OpFreeze)
	@JvmField
	val FALLING_BLOCK = make(fromAngles("wqwawqwqwqwqwqw", EAST), modLoc("falling_block"), OpFallingBlock)
	@JvmField
	val PLACE_TYPE = make(fromAngles("eeeeedeeeee", WEST), modLoc("place_type"), OpPlaceType)
	@JvmField
	val PARTICLES = make(fromAngles("eqqqqa", NORTH_EAST), modLoc("particles"), OpParticles)

	// =============================== Wisp Stuff =====================================
	@JvmField
	val WISP_SUMMON_PROJECTILE = make(fromAngles("aqaeqeeeee", NORTH_WEST), modLoc("wisp/summon/projectile"), OpSummonWisp(false))
	@JvmField
	val WISP_SUMMON_TICKING = make(fromAngles("aqaweewaqawee", NORTH_WEST), modLoc("wisp/summon/ticking"), OpSummonWisp(true))
	@JvmField
	val WISP_SELF = make(fromAngles("dedwqqwdedwqqaw", NORTH_EAST), modLoc("wisp/self"), OpWispSelf)
	@JvmField
	val WISP_MEDIA = make(fromAngles("aqaweewaqaweedw", NORTH_WEST), modLoc("wisp/media"), OpWispMedia)
	@JvmField
	val WISP_HEX = make(fromAngles("aweewaqaweewaawww", SOUTH_EAST), modLoc("wisp/hex"), OpWispHex)
	@JvmField
	val WISP_OWNER = make(fromAngles("dwqqwdedwqqwddwww", SOUTH_WEST), modLoc("wisp/owner"), OpWispOwner)

	// Set and Get Move Target
	@JvmField
	val WISP_MOVE_TARGET_SET = make(fromAngles("awqwawqaw", WEST), modLoc("wisp/move/target/set"), OpMoveTargetSet)
	@JvmField
	val WISP_MOVE_TARGET_GET = make(fromAngles("ewdwewdew", EAST), modLoc("wisp/move/target/get"), OpMoveTargetGet)
	@JvmField
	val WISP_MOVE_SPEED_SET = make(fromAngles("aeawqqqae", WEST), modLoc("wisp/move/speed/set"), OpMoveSpeedSet)
	@JvmField
	val WISP_MOVE_SPEED_GET = make(fromAngles("eeewdqdee", EAST), modLoc("wisp/move/speed/get"), OpMoveSpeedGet)

	// Allow and Disallow Media Transfer
	@JvmField
	val WISP_TRANSFER_ALLOW = make(fromAngles("qqqqqewwqeeeee", NORTH_WEST), modLoc("wisp/transfer/allow"), OpTransferAllowed(true))
	@JvmField
	val WISP_TRANSFER_DISALLOW = make(fromAngles("qqqqqeqdeddweqqqqq", NORTH_WEST), modLoc("wisp/transfer/disallow"), OpTransferAllowed(false))
	@JvmField
	val WISP_TRANSFER_OTHERS_ALLOW = make(
			fromAngles("eeeeeqwweqqqqq", SOUTH_WEST),
			modLoc("wisp/transfer/others/allow"),
			OpTransferAllowedOthers(true))
	@JvmField
	val WISP_TRANSFER_OTHERS_DISALLOW = make(
			fromAngles("eeeeeqeaqaawqeeeee", SOUTH_WEST),
			modLoc("wisp/transfer/others/disallow"),
			OpTransferAllowedOthers(false))

	// Entity purification and Zone distillations
	@JvmField
	val GET_ENTITY_WISP = make(fromAngles("qqwdedwqqdaqaaww", SOUTH_EAST),
		                         modLoc("get_entity/wisp"),
		                         OpGetEntityAt{ entity -> entity is BaseWisp })
	@JvmField
	val ZONE_ENTITY_WISP = make(fromAngles("qqwdedwqqwdeddww", SOUTH_EAST),
		                          modLoc("zone_entity/wisp"),
		                          OpGetEntitiesBy({ entity -> entity is BaseWisp }, false))
	@JvmField
	val ZONE_ENTITY_NOT_WISP = make(fromAngles("eewaqaweewaqaaww", NORTH_EAST),
		                              modLoc("zone_entity/not_wisp"),
		                              OpGetEntitiesBy({ entity -> entity is BaseWisp }, true))

	// Triggers
	@JvmField
	val WISP_TRIGGER_TICK = make(fromAngles("aqawded", NORTH_WEST),
		                           modLoc("wisp/trigger/tick"),
		                           OpWispSetTrigger(WispTriggerTypes.TICK_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_COMM = make(fromAngles("aqqqqqwdeddw", EAST),
		                           modLoc("wisp/trigger/comm"),
		                           OpWispSetTrigger(WispTriggerTypes.COMM_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_MOVE = make(fromAngles("eqwawqwaqww", EAST),
		                           modLoc("wisp/trigger/move"),
		                           OpWispSetTrigger(WispTriggerTypes.MOVE_TRIGGER_TYPE))

	val WISP_SEON_GET = make(fromAngles("daqweewqaeaqweewqaqwwww", EAST),
			modLoc("wisp/seon/get"),
			OpSeonWispGet)

	// =============================== Link Stuff =====================================
	@JvmField
	val LINK = make(fromAngles("eaqaaeqqqqqaweaqaaw", EAST), modLoc("link"), OpLink)
	@JvmField
	val LINK_OTHERS = make(fromAngles("eqqqqqawqeeeeedww", EAST), modLoc("link/others"), OpLinkOthers)
	@JvmField
	val UNLINK = make(fromAngles("qdeddqeeeeedwqdeddw", WEST), modLoc("link/unlink"), OpUnlink)
	@JvmField
	val UNLINK_OTHERS = make(fromAngles("qeeeeedweqqqqqaww", WEST), modLoc("link/unlink/others"), OpUnlinkOthers)
	@JvmField
	val LINK_GET = make(fromAngles("eqqqqqaww", EAST), modLoc("link/get"), OpGetLinked)
	@JvmField
	val LINK_GET_INDEX = make(fromAngles("aeqqqqqawwd", SOUTH_WEST), modLoc("link/get_index"), OpGetLinkedIndex)
	@JvmField
	val LINK_NUM = make(fromAngles("qeeeeedww", WEST), modLoc("link/num"), OpNumLinked)
	@JvmField
	val LINK_COMM_SEND = make(fromAngles("qqqqqwdeddw", NORTH_WEST), modLoc("link/comm/send"), OpSendIota)
	@JvmField
	val LINK_COMM_READ = make(fromAngles("weeeeew", NORTH_EAST), modLoc("link/comm/read"), OpReadReceivedIota)
	@JvmField
	val LINK_COMM_NUM = make(fromAngles("aweeeeewaa", SOUTH_EAST), modLoc("link/comm/num"), OpNumReceivedIota)
	@JvmField
	val LINK_COMM_CLEAR = make(fromAngles("aweeeeewa", SOUTH_EAST), modLoc("link/comm/clear"), OpClearReceivedIotas)
	@JvmField
	val LINK_COMM_OPEN_TRANSMIT = make(fromAngles("qwdedwq", WEST), modLoc("link/comm/open_transmit"), OpOpenTransmit)
	@JvmField
	val LINK_COMM_CLOSE_TRANSMIT = make(fromAngles("ewaqawe", EAST), modLoc("link/comm/close_transmit"), OpCloseTransmit)

	// =============================== Gate Stuff =====================================
	@JvmField
	val GATE_MARK = make(fromAngles("qaqeede", WEST), modLoc("gate/mark"), OpMarkGate)
	@JvmField
	val GATE_UNMARK = make(fromAngles("edeqqaq", EAST), modLoc("gate/unmark"), OpUnmarkGate)
	@JvmField
	val GATE_MARK_GET = make(fromAngles("edwwdeeede", EAST), modLoc("gate/mark/get"), OpGetMarkedGate)
	@JvmField
	val GATE_MARK_NUM_GET = make(fromAngles("qawwaqqqaq", WEST), modLoc("gate/mark/num/get"), OpGetNumMarkedGate)
	@JvmField
	val GATE_CLOSE = make(fromAngles("qqqwwqqqwqqawdedw", WEST), modLoc("gate/close"), OpCloseGate)

	// =============================== Gate Stuff =====================================
	@JvmField
	val BIND_STORAGE = make(fromAngles("qaqwqaqwqaq", NORTH_WEST), modLoc("mote/storage/bind"), OpBindStorage(false))
	@JvmField
	val BIND_STORAGE_TEMP = make(fromAngles("edewedewede", NORTH_EAST), modLoc("mote/storage/bind/temp"), OpBindStorage(true))
	@JvmField
	val MOTE_CONTAINED_TYPE_GET = make(fromAngles("dwqqqqqwddww", NORTH_EAST), modLoc("mote/contained_type/get"), OpGetContainedItemTypes)
	@JvmField
	val MOTE_CONTAINED_GET = make(fromAngles("aweeeeewaaww", SOUTH_EAST), modLoc("mote/contained/get"), OpGetContainedMotes)
	@JvmField
	val MOTE_STORAGE_REMAINING_CAPACITY_GET = make(fromAngles("awedqdewa", SOUTH_EAST), modLoc("mote/storage/remaining_capacity/get"), OpGetStorageRemainingCapacity)
	@JvmField
	val MOTE_STORAGE_CONTAINS = make(fromAngles("dwqaeaqwd", NORTH_EAST), modLoc("mote/storage/contains"), OpStorageContains)
	@JvmField
	val MOTE_MAKE = make(fromAngles("eaqa", WEST), modLoc("mote/make"), OpMakeMote)
	@JvmField
	val MOTE_RETURN = make(fromAngles("qded", EAST), modLoc("mote/return"), OpReturnMote)
	@JvmField
	val MOTE_COUNT_GET = make(fromAngles("qqqqwqqqqqaa", NORTH_WEST), modLoc("mote/count/get"), OpGetCountMote)
	@JvmField
	val MOTE_COMBINE = make(fromAngles("aqaeqded", NORTH_WEST), modLoc("mote/combine"), OpCombineMotes)
	@JvmField
	val MOTE_COMBINABLE = make(fromAngles("dedqeaqa", SOUTH_WEST), modLoc("mote/combinable"), OpMotesCombinable)
	@JvmField
	val MOTE_SPLIT = make(fromAngles("eaqaaw", EAST), modLoc("mote/split"), OpSplitMote)
	@JvmField
	val MOTE_STORAGE_GET = make(fromAngles("qqqqqaw", SOUTH_WEST), modLoc("mote/storage/get"), OpGetMoteStorage)
	@JvmField
	val MOTE_STORAGE_SET = make(fromAngles("eeeeedw", SOUTH_EAST), modLoc("mote/storage/set"), OpSetMoteStorage)
	@JvmField
	val MOTE_CRAFT = make(fromAngles("wwawdedwawdewwdwaqawdwwedwawdedwaww", SOUTH_EAST), modLoc("mote/craft"), OpCraftMote)
	@JvmField
	val MOTE_VILLAGER_LEVEL_GET = make(fromAngles("qqwdedwqqaww", NORTH_WEST), modLoc("mote/villager/level/get"), OpGetVillagerLevel)
	@JvmField
	val MOTE_TRADE_GET = make(fromAngles("awdedwaawwqded", SOUTH_EAST), modLoc("mote/trade/get"), OpGetItemTrades)
	@JvmField
	val MOTE_TRADE = make(fromAngles("awdedwaeqded", NORTH_WEST), modLoc("mote/trade"), OpTradeMote)
	@JvmField
	val MOTE_USE_ON = make(fromAngles("qqqwqqqqaa", EAST), modLoc("mote/use_on"), OpUseMoteOn)

	// ============================== Great Stuff =====================================
	@JvmField
	val CONSUME_WISP = make(fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", NORTH_WEST),
			modLoc("wisp/consume"),
			OpConsumeWisp,
			true)
	@JvmField
	val WISP_SEON_SET = make(fromAngles("aqweewqaeaqweewqaqwww", SOUTH_WEST),
			modLoc("wisp/seon/set"),
			OpSeonWispSet,
			true)
	@JvmField
	val TICK = make(fromAngles("wwwdwdwwwawqqeqwqqwqeqwqq", SOUTH_EAST), modLoc("tick"), OpTick, true)
	@JvmField
	val GATE_MAKE = make(fromAngles("qwqwqwqwqwqqeaeaeaeaeae", WEST), modLoc("gate/make"), OpMakeGate, true)

	// ================================ Special Handlers =======================================
//	@JvmField
//	val EXAMPLE_HANDLER = make(modLoc("example_handler")) {pat ->
//		return@make Action.makeConstantOp(StringIota("example! $pat"))
//	}

	fun make (pattern: HexPattern, location: ResourceLocation, operator: Action, isPerWorld: Boolean = false): PatternIota {
		val triple = Triple(pattern, location, operator)
		if (isPerWorld)
			PER_WORLD_PATTERNS.add(triple)
		else
			PATTERNS.add(triple)
		return PatternIota(pattern)
	}

	fun make (location: ResourceLocation, specialHandler: PatternRegistry.SpecialHandler): Pair<ResourceLocation, PatternRegistry.SpecialHandler> {
		val pair = location to specialHandler
		SPECIAL_HANDLERS.add(pair)
		return pair
	}
}