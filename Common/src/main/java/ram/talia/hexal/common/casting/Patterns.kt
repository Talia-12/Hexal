@file:Suppress("unused")

package ram.talia.hexal.common.casting

import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.PatternIota
import at.petrak.hexcasting.api.spell.iota.Vec3Iota
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
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
import ram.talia.hexal.common.casting.actions.spells.items.*
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
	val TYPE_BLOCK_ITEM = make(HexPattern.fromAngles("qaqqaea", HexDir.EAST), modLoc("type/block_item"), OpTypeBlockItem)
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
	val HEALTH = make(HexPattern.fromAngles("aqwawqa", HexDir.NORTH_WEST), modLoc("health"), OpGetHealth)
	@JvmField
	val ARMOUR = make(HexPattern.fromAngles("wqqqqw", HexDir.NORTH_WEST), modLoc("armour"), OpGetArmour)
	@JvmField
	val TOUGHNESS = make(HexPattern.fromAngles("aeqqqqea", HexDir.EAST), modLoc("toughness"), OpGetToughness)
	@JvmField
	val LIGHT_LEVEL = make(HexPattern.fromAngles("qedqde", HexDir.NORTH_EAST), modLoc("light_level"), OpGetLightLevel)

	// =============================== Misc Maths =====================================
	@JvmField
	val FACTORIAL = make(HexPattern.fromAngles("wawdedwaw", HexDir.SOUTH_EAST), modLoc("factorial"), OpFactorial)
	@JvmField
	val RUNNING_SUM = make(HexPattern.fromAngles("aea", HexDir.WEST), modLoc("running/sum"), OpRunningOp ({ if (it is Vec3Iota) Vec3Iota(Vec3.ZERO) else DoubleIota(0.0) })
	{ running, iota ->
		when (running) {
			is DoubleIota -> DoubleIota(running.double + ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
			is Vec3Iota -> Vec3Iota(running.vec3 + ((iota as? Vec3Iota)?.vec3 ?: throw OpRunningOp.InvalidIotaException("list.vec")))
			else -> throw OpRunningOp.InvalidIotaException("list.doublevec")
		}
	})
	@JvmField
	val RUNNING_MUL = make(HexPattern.fromAngles("qaawaaq", HexDir.NORTH_EAST), modLoc("running/mul"), OpRunningOp ({ DoubleIota(1.0) })
	{ running, iota ->
		if (running !is DoubleIota)
			throw OpRunningOp.InvalidIotaException("list.double")
		DoubleIota(running.double * ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
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
	@JvmField
	val PARTICLES = make(HexPattern.fromAngles("eqqqqa", HexDir.NORTH_EAST), modLoc("particles"), OpParticles)

	// =============================== Wisp Stuff =====================================
	@JvmField
	val WISP_SUMMON_PROJECTILE = make(HexPattern.fromAngles("aqaeqeeeee", HexDir.NORTH_WEST), modLoc("wisp/summon/projectile"), OpSummonWisp(false))
	@JvmField
	val WISP_SUMMON_TICKING = make(HexPattern.fromAngles("aqaweewaqawee", HexDir.NORTH_WEST), modLoc("wisp/summon/ticking"), OpSummonWisp(true))
	@JvmField
	val WISP_SELF = make(HexPattern.fromAngles("dedwqqwdedwqqaw", HexDir.NORTH_EAST), modLoc("wisp/self"), OpWispSelf)
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

	// =============================== Link Stuff =====================================
	@JvmField
	val LINK = make(HexPattern.fromAngles("eaqaaeqqqqqaweaqaaw", HexDir.EAST), modLoc("link"), OpLink)
	@JvmField
	val LINK_OTHERS = make(HexPattern.fromAngles("eqqqqqawqeeeeedww", HexDir.EAST), modLoc("link/others"), OpLinkOthers)
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
	val LINK_COMM_CLEAR = make(HexPattern.fromAngles("aweeeeewa", HexDir.SOUTH_EAST), modLoc("link/comm/clear"), OpClearReceivedIotas)
	@JvmField
	val LINK_COMM_OPEN_TRANSMIT = make(HexPattern.fromAngles("qwdedwq", HexDir.WEST), modLoc("link/comm/open_transmit"), OpOpenTransmit)
	@JvmField
	val LINK_COMM_CLOSE_TRANSMIT = make(HexPattern.fromAngles("ewaqawe", HexDir.EAST), modLoc("link/comm/close_transmit"), OpCloseTransmit)

	// =============================== Gate Stuff =====================================
	@JvmField
	val GATE_MARK = make(HexPattern.fromAngles("qaqeede", HexDir.WEST), modLoc("gate/mark"), OpMarkGate)
	@JvmField
	val GATE_MARK_GET = make(HexPattern.fromAngles("edwwdeeede", HexDir.EAST), modLoc("gate/mark/get"), OpGetMarkedGate)
	@JvmField
	val GATE_MARK_NUM_GET = make(HexPattern.fromAngles("qawwaqqqaq", HexDir.WEST), modLoc("gate/mark/num/get"), OpGetNumMarkedGate)
	@JvmField
	val GATE_CLOSE = make(HexPattern.fromAngles("qqqwwqqqwqqawdedw", HexDir.WEST), modLoc("gate/close"), OpCloseGate)

	// =============================== Gate Stuff =====================================
	@JvmField
	val BIND_STORAGE = make(HexPattern.fromAngles("qaqwqaqwqaq", HexDir.NORTH_WEST), modLoc("item/storage/bind"), OpBindStorage(false))
	@JvmField
	val BIND_STORAGE_TEMP = make(HexPattern.fromAngles("edewedewede", HexDir.NORTH_EAST), modLoc("item/storage/bind/temp"), OpBindStorage(true))
	@JvmField
	val ITEM_CONTAINED_TYPE_GET = make(HexPattern.fromAngles("dwqqqqqwddww", HexDir.NORTH_EAST), modLoc("item/contained_type/get"), OpGetContainedItemTypes)
	@JvmField
	val ITEM_CONTAINED_GET = make(HexPattern.fromAngles("aweeeeewaaww", HexDir.SOUTH_EAST), modLoc("item/contained/get"), OpGetContainedItems)
	@JvmField
	val ITEM_STORAGE_REMAINING_CAPACITY_GET = make(HexPattern.fromAngles("awedqdewa", HexDir.SOUTH_EAST), modLoc("item/storage/remaining_capacity/get"), OpGetStorageRemainingCapacity)
	@JvmField
	val ITEM_MAKE = make(HexPattern.fromAngles("eaqa", HexDir.WEST), modLoc("item/make"), OpMakeItem)
	@JvmField
	val ITEM_RETURN = make(HexPattern.fromAngles("qded", HexDir.EAST), modLoc("item/return"), OpReturnItem)
	@JvmField
	val ITEM_COMBINE = make(HexPattern.fromAngles("aqaeqded", HexDir.NORTH_WEST), modLoc("item/combine"), OpCombineItems)
	@JvmField
	val ITEM_SPLIT = make(HexPattern.fromAngles("eaqaaw", HexDir.EAST), modLoc("item/split"), OpSplitItem)
	@JvmField
	val ITEM_STORAGE_GET = make(HexPattern.fromAngles("qqqqqaw", HexDir.SOUTH_WEST), modLoc("item/storage/get"), OpGetItemStorage)
	@JvmField
	val ITEM_STORAGE_SET = make(HexPattern.fromAngles("eeeeedw", HexDir.SOUTH_EAST), modLoc("item/storage/set"), OpSetItemStorage)
	@JvmField
	val ITEM_CRAFT = make(HexPattern.fromAngles("wwawdedwawdewwdwaqawdwwedwawdedwaww", HexDir.SOUTH_EAST), modLoc("item/craft"), OpCraftItem)
	@JvmField
	val ITEM_VILLAGER_LEVEL_GET = make(HexPattern.fromAngles("qqwdedwqqaww", HexDir.NORTH_WEST), modLoc("item/villager/level/get"), OpGetVillagerLevel)
	@JvmField
	val ITEM_TRADE_GET = make(HexPattern.fromAngles("awdedwaawwqded", HexDir.SOUTH_EAST), modLoc("item/trade/get"), OpGetItemTrades)
	@JvmField
	val ITEM_TRADE = make(HexPattern.fromAngles("awdedwaeqded", HexDir.NORTH_WEST), modLoc("item/trade"), OpTradeItem)


	// ============================== Great Stuff =====================================
	@JvmField
	val CONSUME_WISP = make(HexPattern.fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
			modLoc("wisp/consume"),
			OpConsumeWisp,
			true)
	@JvmField
	val WISP_SEON_SET = make(HexPattern.fromAngles("aqweewqaeaqweewqaqwww", HexDir.SOUTH_WEST),
			modLoc("wisp/seon/set"),
			OpSeonWispSet,
			true)
	@JvmField
	val TICK = make(HexPattern.fromAngles("wwwdwdwwwawqqeqwqqwqeqwqq", HexDir.SOUTH_EAST), modLoc("tick"), OpTick, true)
	@JvmField
	val GATE_MAKE = make(HexPattern.fromAngles("qwqwqwqwqwqqeaeaeaeaeae", HexDir.WEST), modLoc("gate/make"), OpMakeGate, true)

	// ================================ Special Handlers =======================================
//	@JvmField
//	val EXAMPLE_HANDLER = make(modLoc("example_handler")) {pat ->
//		return@make Action.makeConstantOp(StringIota("example! $pat"))
//	}

	private fun make (pattern: HexPattern, location: ResourceLocation, operator: Action, isPerWorld: Boolean = false): PatternIota {
		val triple = Triple(pattern, location, operator)
		if (isPerWorld)
			PER_WORLD_PATTERNS.add(triple)
		else
			PATTERNS.add(triple)
		return PatternIota(pattern)
	}

	private fun make (location: ResourceLocation, specialHandler: PatternRegistry.SpecialHandler): Pair<ResourceLocation, PatternRegistry.SpecialHandler> {
		val pair = location to specialHandler
		SPECIAL_HANDLERS.add(pair)
		return pair
	}
}