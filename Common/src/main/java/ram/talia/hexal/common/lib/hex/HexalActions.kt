@file:Suppress("unused")

package ram.talia.hexal.common.lib.hex

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.OperationAction
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntitiesBy
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntityAt
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.casting.wisp.triggers.WispTriggerTypes
import ram.talia.hexal.api.plus
import ram.talia.hexal.common.casting.actions.*
import ram.talia.hexal.common.casting.actions.everbook.OpEverbookDelete
import ram.talia.hexal.common.casting.actions.everbook.OpEverbookRead
import ram.talia.hexal.common.casting.actions.everbook.OpEverbookWrite
import ram.talia.hexal.common.casting.actions.everbook.OpToggleMacro
import ram.talia.hexal.common.casting.actions.spells.*
import ram.talia.hexal.common.casting.actions.spells.gates.*
import ram.talia.hexal.common.casting.actions.spells.great.OpConsumeWisp
import ram.talia.hexal.common.casting.actions.spells.great.OpSeonWispSet
import ram.talia.hexal.common.casting.actions.spells.great.OpTick
import ram.talia.hexal.common.casting.actions.spells.link.*
import ram.talia.hexal.common.casting.actions.spells.motes.*
import ram.talia.hexal.common.casting.actions.spells.wisp.*
import ram.talia.hexal.common.entities.BaseWisp
import java.util.function.BiConsumer

object HexalActions {

	private val ACTIONS: MutableMap<ResourceLocation, ActionRegistryEntry> = mutableMapOf()

	@JvmStatic
	fun register(r: BiConsumer<ActionRegistryEntry, ResourceLocation>) {
		for ((key, value) in ACTIONS.entries) {
			r.accept(value, key)
		}
	}

	// ========================== Misc Info Gathering =================================
	@JvmField
	val CURRENT_TICK = make("current_tick", HexPattern.fromAngles("ddwaa", HexDir.NORTH_WEST), OpCurrentTick)
	@JvmField
	val REMAINING_EVALS = make("remaining_evals", HexPattern.fromAngles("qqaed", HexDir.SOUTH_EAST), OpRemainingOps)
	@JvmField
	val BREATH = make("breath", HexPattern.fromAngles("aqawdwaqawd", HexDir.NORTH_WEST), OpGetBreath)
	@JvmField
	val HEALTH = make("health", HexPattern.fromAngles("aqwawqa", HexDir.NORTH_WEST), OpGetHealth)
	@JvmField
	val ARMOUR = make("armour", HexPattern.fromAngles("wqqqqw", HexDir.NORTH_WEST), OpGetArmour)
	@JvmField
	val TOUGHNESS = make("toughness", HexPattern.fromAngles("aeqqqqea", HexDir.EAST), OpGetToughness)
	@JvmField
	val LIGHT_LEVEL = make("light_level", HexPattern.fromAngles("qedqde", HexDir.NORTH_EAST), OpGetLightLevel)

	// =============================== Misc Maths =====================================
	@JvmField
	val FACTORIAL = make("factorial", HexPattern.fromAngles("wawdedwaw", HexDir.SOUTH_EAST), OpFactorial)
	@JvmField
	val RUNNING_SUM = make("running/sum", HexPattern.fromAngles("aea", HexDir.WEST), OpRunningOp ({ if (it is Vec3Iota) Vec3Iota(Vec3.ZERO) else DoubleIota(0.0) })
	{ running, iota ->
		when (running) {
			is DoubleIota -> DoubleIota(running.double + ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
			is Vec3Iota -> Vec3Iota(running.vec3 + ((iota as? Vec3Iota)?.vec3 ?: throw OpRunningOp.InvalidIotaException("list.vec")))
			else -> throw OpRunningOp.InvalidIotaException("list.doublevec")
		}
	})
	@JvmField
	val RUNNING_MUL = make("running/mul", HexPattern.fromAngles("qaawaaq", HexDir.NORTH_EAST), OpRunningOp ({ DoubleIota(1.0) })
	{ running, iota ->
		if (running !is DoubleIota)
			throw OpRunningOp.InvalidIotaException("list.double")
		DoubleIota(running.double * ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
	})

	// ================================ Everbook ======================================
	@JvmField
	val EVERBOOK_READ = make("everbook/read", HexPattern.fromAngles("eweeewedqdeddw", HexDir.NORTH_EAST), OpEverbookRead)
	@JvmField
	val EVERBOOK_WRITE = make("everbook/write", HexPattern.fromAngles("qwqqqwqaeaqaaw", HexDir.SOUTH_EAST), OpEverbookWrite)
	@JvmField
	val EVERBOOK_DELETE = make("everbook/delete", HexPattern.fromAngles("qwqqqwqaww", HexDir.SOUTH_EAST), OpEverbookDelete)
	@JvmField
	val EVERBOOK_TOGGLE_MACRO = make("everbook/toggle_macro", HexPattern.fromAngles("eweeewedww", HexDir.SOUTH_WEST), OpToggleMacro)

	// ============================== Misc Spells =====================================
	@JvmField
	val SMELT = make("smelt", HexPattern.fromAngles("wqqqwqqadad", HexDir.EAST), OpSmelt)
	@JvmField
	val FREEZE = make("freeze", HexPattern.fromAngles("weeeweedada", HexDir.WEST), OpFreeze)
	@JvmField
	val FALLING_BLOCK = make("falling_block", HexPattern.fromAngles("wqwawqwqwqwqwqw", HexDir.EAST), OpFallingBlock)
	@JvmField
	val PLACE_TYPE = make("place_type", HexPattern.fromAngles("eeeeedeeeee", HexDir.WEST), OpPlaceType)
	@JvmField
	val PARTICLES = make("particles", HexPattern.fromAngles("eqqqqa", HexDir.NORTH_EAST), OpParticles)

	// =============================== Wisp Stuff =====================================
	@JvmField
	val WISP_SUMMON_PROJECTILE = make("wisp/summon/projectile", HexPattern.fromAngles("aqaeqeeeee", HexDir.NORTH_WEST), OpSummonWisp(false))
	@JvmField
	val WISP_SUMMON_TICKING = make("wisp/summon/ticking", HexPattern.fromAngles("aqaweewaqawee", HexDir.NORTH_WEST), OpSummonWisp(true))
	@JvmField
	val WISP_SELF = make("wisp/self", HexPattern.fromAngles("dedwqqwdedwqqaw", HexDir.NORTH_EAST), OpWispSelf)
	@JvmField
	val WISP_MEDIA = make("wisp/media", HexPattern.fromAngles("aqaweewaqaweedw", HexDir.NORTH_WEST), OpWispMedia)
	@JvmField
	val WISP_HEX = make("wisp/hex", HexPattern.fromAngles("aweewaqaweewaawww", HexDir.SOUTH_EAST), OpWispHex)
	@JvmField
	val WISP_OWNER = make("wisp/owner", HexPattern.fromAngles("dwqqwdedwqqwddwww", HexDir.SOUTH_WEST), OpWispOwner)

	// Set and Get Move Target
	@JvmField
	val WISP_MOVE_TARGET_SET = make("wisp/move/target/set", HexPattern.fromAngles("awqwawqaw", HexDir.WEST), OpMoveTargetSet)
	@JvmField
	val WISP_MOVE_TARGET_GET = make("wisp/move/target/get", HexPattern.fromAngles("ewdwewdew", HexDir.EAST), OpMoveTargetGet)
	@JvmField
	val WISP_MOVE_SPEED_SET = make("wisp/move/speed/set", HexPattern.fromAngles("aeawqqqae", HexDir.WEST), OpMoveSpeedSet)
	@JvmField
	val WISP_MOVE_SPEED_GET = make("wisp/move/speed/get", HexPattern.fromAngles("eeewdqdee", HexDir.EAST), OpMoveSpeedGet)

	// Allow and Disallow Media Transfer
	@JvmField
	val WISP_TRANSFER_ALLOW = make("wisp/transfer/allow", HexPattern.fromAngles("qqqqqewwqeeeee", HexDir.NORTH_WEST), OpTransferAllowed(true))
	@JvmField
	val WISP_TRANSFER_DISALLOW = make("wisp/transfer/disallow", HexPattern.fromAngles("qqqqqeqdeddweqqqqq", HexDir.NORTH_WEST), OpTransferAllowed(false))
	@JvmField
	val WISP_TRANSFER_OTHERS_ALLOW = make(
			"wisp/transfer/others/allow",
			HexPattern.fromAngles("eeeeeqwweqqqqq", HexDir.SOUTH_WEST),
			OpTransferAllowedOthers(true))
	@JvmField
	val WISP_TRANSFER_OTHERS_DISALLOW = make(
			"wisp/transfer/others/disallow",
			HexPattern.fromAngles("eeeeeqeaqaawqeeeee", HexDir.SOUTH_WEST),
			OpTransferAllowedOthers(false))

	// Entity purification and Zone distillations
	@JvmField
	val GET_ENTITY_WISP = make("get_entity/wisp",
			HexPattern.fromAngles("qqwdedwqqdaqaaww", HexDir.SOUTH_EAST),
		 	OpGetEntityAt{ entity -> entity is BaseWisp })
	@JvmField
	val ZONE_ENTITY_WISP = make("zone_entity/wisp",
			HexPattern.fromAngles("qqwdedwqqwdeddww", HexDir.SOUTH_EAST),
		  	OpGetEntitiesBy({ entity -> entity is BaseWisp }, false))
	@JvmField
	val ZONE_ENTITY_NOT_WISP = make("zone_entity/not_wisp",
			HexPattern.fromAngles("eewaqaweewaqaaww", HexDir.NORTH_EAST),
			OpGetEntitiesBy({ entity -> entity is BaseWisp }, true))

	// Triggers
	@JvmField
	val WISP_TRIGGER_TICK = make("wisp/trigger/tick",
			HexPattern.fromAngles("aqawded", HexDir.NORTH_WEST),
		    OpWispSetTrigger(WispTriggerTypes.TICK_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_COMM = make("wisp/trigger/comm",
			HexPattern.fromAngles("aqqqqqwdeddw", HexDir.EAST),
		    OpWispSetTrigger(WispTriggerTypes.COMM_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_MOVE = make("wisp/trigger/move",
			HexPattern.fromAngles("eqwawqwaqww", HexDir.EAST),
		    OpWispSetTrigger(WispTriggerTypes.MOVE_TRIGGER_TYPE))

	val WISP_SEON_GET = make("wisp/seon/get",
			HexPattern.fromAngles("daqweewqaeaqweewqaqwwww", HexDir.EAST),
			OpSeonWispGet)

	// =============================== Link Stuff =====================================
	@JvmField
	val LINK = make("link", HexPattern.fromAngles("eaqaaeqqqqqaweaqaaw", HexDir.EAST), OpLink)
	@JvmField
	val LINK_OTHERS = make("link/others", HexPattern.fromAngles("eqqqqqawqeeeeedww", HexDir.EAST), OpLinkOthers)
	@JvmField
	val UNLINK = make("link/unlink", HexPattern.fromAngles("qdeddqeeeeedwqdeddw", HexDir.WEST), OpUnlink)
	@JvmField
	val UNLINK_OTHERS = make("link/unlink/others", HexPattern.fromAngles("qeeeeedweqqqqqaww", HexDir.WEST), OpUnlinkOthers)
	@JvmField
	val LINK_GET = make("link/get", HexPattern.fromAngles("eqqqqqaww", HexDir.EAST), OpGetLinked)
	@JvmField
	val LINK_GET_INDEX = make("link/get_index", HexPattern.fromAngles("aeqqqqqawwd", HexDir.SOUTH_WEST), OpGetLinkedIndex)
	@JvmField
	val LINK_NUM = make("link/num", HexPattern.fromAngles("qeeeeedww", HexDir.WEST), OpNumLinked)
	@JvmField
	val LINK_COMM_SEND = make("link/comm/send", HexPattern.fromAngles("qqqqqwdeddw", HexDir.NORTH_WEST), OpSendIota)
	@JvmField
	val LINK_COMM_READ = make("link/comm/read", HexPattern.fromAngles("weeeeew", HexDir.NORTH_EAST), OpReadReceivedIota)
	@JvmField
	val LINK_COMM_NUM = make("link/comm/num", HexPattern.fromAngles("aweeeeewaa", HexDir.SOUTH_EAST), OpNumReceivedIota)
	@JvmField
	val LINK_COMM_CLEAR = make("link/comm/clear", HexPattern.fromAngles("aweeeeewa", HexDir.SOUTH_EAST), OpClearReceivedIotas)
	@JvmField
	val LINK_COMM_OPEN_TRANSMIT = make("link/comm/open_transmit", HexPattern.fromAngles("qwdedwq", HexDir.WEST), OpOpenTransmit)
	@JvmField
	val LINK_COMM_CLOSE_TRANSMIT = make("link/comm/close_transmit", HexPattern.fromAngles("ewaqawe", HexDir.EAST), OpCloseTransmit)

	// =============================== Gate Stuff =====================================
	@JvmField
	val GATE_MARK = make("gate/mark", HexPattern.fromAngles("qaqeede", HexDir.WEST), OpMarkGate)
	@JvmField
	val GATE_UNMARK = make("gate/unmark", HexPattern.fromAngles("edeqqaq", HexDir.EAST), OpUnmarkGate)
	@JvmField
	val GATE_MARK_GET = make("gate/mark/get", HexPattern.fromAngles("edwwdeeede", HexDir.EAST), OpGetMarkedGate)
//	@JvmField
//	val GATE_MARK_NUM_GET = make("gate/mark/num/get", HexPattern.fromAngles("qawwaqqqaq", HexDir.WEST), OpGetNumMarkedGate)
	@JvmField
	val GATE_CLOSE = make("gate/close", HexPattern.fromAngles("qqqwwqqqwqqawdedw", HexDir.WEST), OpCloseGate)

	// =============================== Gate Stuff =====================================
	@JvmField
	val BIND_STORAGE = make("mote/storage/bind", HexPattern.fromAngles("qaqwqaqwqaq", HexDir.NORTH_WEST), OpBindStorage(false))
	@JvmField
	val BIND_STORAGE_TEMP = make("mote/storage/bind/temp", HexPattern.fromAngles("edewedewede", HexDir.NORTH_EAST), OpBindStorage(true))
	@JvmField
	val MOTE_CONTAINED_TYPE_GET = make("mote/contained_type/get", HexPattern.fromAngles("dwqqqqqwddww", HexDir.NORTH_EAST), OpGetContainedItemTypes)
	@JvmField
	val MOTE_CONTAINED_GET = make("mote/contained/get", HexPattern.fromAngles("aweeeeewaaww", HexDir.SOUTH_EAST), OpGetContainedMotes)
	@JvmField
	val MOTE_STORAGE_REMAINING_CAPACITY_GET = make("mote/storage/remaining_capacity/get", HexPattern.fromAngles("awedqdewa", HexDir.SOUTH_EAST), OpGetStorageRemainingCapacity)
	@JvmField
	val MOTE_STORAGE_CONTAINS = make("mote/storage/contains", HexPattern.fromAngles("dwqaeaqwd", HexDir.NORTH_EAST), OpStorageContains)
	@JvmField
	val MOTE_MAKE = make("mote/make", HexPattern.fromAngles("eaqa", HexDir.WEST), OpMakeMote)
	@JvmField
	val MOTE_RETURN = make("mote/return", HexPattern.fromAngles("qded", HexDir.EAST), OpReturnMote)
//	@JvmField
//	val MOTE_COUNT_GET = make("mote/count/get", HexPattern.fromAngles("qqqqwqqqqqaa", HexDir.NORTH_WEST), OpGetCountMote)
//	@JvmField
//	val MOTE_COMBINE = make("mote/combine", HexPattern.fromAngles("aqaeqded", HexDir.NORTH_WEST), OpCombineMotes)
	@JvmField
	val MOTE_COMBINABLE = make("mote/combinable", HexPattern.fromAngles("dedqeaqa", HexDir.SOUTH_WEST), OpMotesCombinable)
	@JvmField
	val MOTE_SPLIT = make("mote/split", HexPattern.fromAngles("eaqaaw", HexDir.EAST), OpSplitMote)
	@JvmField
	val MOTE_STORAGE_GET = make("mote/storage/get", HexPattern.fromAngles("qqqqqaw", HexDir.SOUTH_WEST), OpGetMoteStorage)
	@JvmField
	val MOTE_STORAGE_SET = make("mote/storage/set", HexPattern.fromAngles("eeeeedw", HexDir.SOUTH_EAST), OpSetMoteStorage)
	@JvmField
	val MOTE_CRAFT = make("mote/craft", HexPattern.fromAngles("wwawdedwawdewwdwaqawdwwedwawdedwaww", HexDir.SOUTH_EAST), OpCraftMote)
	@JvmField
	val MOTE_VILLAGER_LEVEL_GET = make("mote/villager/level/get", HexPattern.fromAngles("qqwdedwqqaww", HexDir.NORTH_WEST), OpGetVillagerLevel)
	@JvmField
	val MOTE_TRADE_GET = make("mote/trade/get", HexPattern.fromAngles("awdedwaawwqded", HexDir.SOUTH_EAST), OpGetItemTrades)
	@JvmField
	val MOTE_TRADE = make("mote/trade", HexPattern.fromAngles("awdedwaeqded", HexDir.NORTH_WEST), OpTradeMote)
	@JvmField
	val MOTE_USE_ON = make("mote/use_on", HexPattern.fromAngles("qqqwqqqqaa", HexDir.EAST), OpUseMoteOn)

	// ============================== Great Stuff =====================================
	@JvmField
	val CONSUME_WISP = make("wisp/consume",
			HexPattern.fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
			OpConsumeWisp)
	@JvmField
	val WISP_SEON_SET = make("wisp/seon/set",
			HexPattern.fromAngles("aqweewqaeaqweewqaqwww", HexDir.SOUTH_WEST),
			OpSeonWispSet)
	@JvmField
	val TICK = make("tick", HexPattern.fromAngles("wwwdwdwwwawqqeqwqqwqeqwqq", HexDir.SOUTH_EAST), OpTick)
	@JvmField
	val GATE_MAKE = make("gate/make", HexPattern.fromAngles("qwqwqwqwqwqqeaeaeaeaeae", HexDir.WEST), OpMakeGate)



	fun make(name: String, pattern: HexPattern, action: Action): ActionRegistryEntry = make(name, ActionRegistryEntry(pattern, action))

	fun make(name: String, are: ActionRegistryEntry): ActionRegistryEntry {
		return if (ACTIONS.put(modLoc(name), are) != null) {
			throw IllegalArgumentException("Typo? Duplicate id $name")
		} else {
			are
		}
	}

	fun make(name: String, oa: OperationAction): ActionRegistryEntry {
		val are = ActionRegistryEntry(oa.pattern, oa)
		return if (ACTIONS.put(modLoc(name), are) != null) {
			throw IllegalArgumentException("Typo? Duplicate id $name")
		} else {
			are
		}
	}

}