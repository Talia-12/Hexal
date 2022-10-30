package ram.talia.hexal.common.casting;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntitiesBy;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntityAt;
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerTypes;
import ram.talia.hexal.common.casting.actions.*;
import ram.talia.hexal.common.casting.actions.everbook.OpEverbookDelete;
import ram.talia.hexal.common.casting.actions.everbook.OpEverbookRead;
import ram.talia.hexal.common.casting.actions.everbook.OpEverbookWrite;
import ram.talia.hexal.common.casting.actions.everbook.OpToggleMacro;
import ram.talia.hexal.common.casting.actions.spells.OpFallingBlock;
import ram.talia.hexal.common.casting.actions.spells.OpFreeze;
import ram.talia.hexal.common.casting.actions.spells.OpSmelt;
import ram.talia.hexal.common.casting.actions.spells.link.*;
import ram.talia.hexal.common.casting.actions.spells.wisp.*;
import ram.talia.hexal.common.casting.actions.spells.great.OpConsumeWisp;
import ram.talia.hexal.common.entities.BaseWisp;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			
			// ============================ Type Comparison ===================================
			PatternRegistry.mapPattern(HexPattern.fromAngles("qaqqaea", HexDir.EAST),
																 modLoc("compare/blocks"),
																 OpCompareBlocks.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qawde", HexDir.SOUTH_WEST),
																 modLoc("compare/entities"),
																 OpCompareEntities.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("awd", HexDir.SOUTH_WEST),
																 modLoc("compare/types"),
																 OpCompareTypes.INSTANCE);
			
			// ========================== Misc Info Gathering =================================
			
			PatternRegistry.mapPattern(HexPattern.fromAngles("ddwaa", HexDir.NORTH_WEST),
																 modLoc("current_tick"),
																 OpCurrentTick.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("eedqa", HexDir.WEST),
																 modLoc("remaining_evals"),
																 OpRemainingEvals.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqawdwaqawd", HexDir.NORTH_WEST),
																 modLoc("breath"),
																 OpGetBreath.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqwawqa", HexDir.NORTH_WEST),
																 modLoc("health"),
																 OpGetHealth.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qedqde", HexDir.NORTH_EAST),
																 modLoc("light_level"),
																 OpGetLightLevel.INSTANCE);
			
			// =============================== Misc Maths =====================================
			
			PatternRegistry.mapPattern(HexPattern.fromAngles("wawdedwaw", HexDir.SOUTH_EAST),
																 modLoc("factorial"),
																 OpFactorial.INSTANCE);
			
			// ================================ Everbook ======================================
			
			PatternRegistry.mapPattern(HexPattern.fromAngles("eweeewedqdeddw", HexDir.NORTH_EAST),
																 modLoc("everbook/read"),
																 OpEverbookRead.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qwqqqwqaeaqaaw", HexDir.SOUTH_EAST),
																 modLoc("everbook/write"),
																 OpEverbookWrite.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qwqqqwqaww", HexDir.SOUTH_EAST),
																 modLoc("everbook/delete"),
																 OpEverbookDelete.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("eweeewedww", HexDir.SOUTH_WEST),
																 modLoc("everbook/toggle_macro"),
																 OpToggleMacro.INSTANCE);
			
			// ============================== Misc Spells =====================================
			
			PatternRegistry.mapPattern(HexPattern.fromAngles("wqqqwqqadad", HexDir.EAST),
																 modLoc("smelt"),
																 OpSmelt.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("weeeweedada", HexDir.WEST),
																 modLoc("freeze"),
																 OpFreeze.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("wqwawqwqwqwqwqw", HexDir.EAST),
																 modLoc("falling_block"),
																 OpFallingBlock.INSTANCE);
			
			// =============================== Wisp Stuff =====================================

			PatternRegistry.mapPattern(HexPattern.fromAngles("aqaeqeeeee", HexDir.NORTH_WEST),
																 modLoc("wisp/summon/projectile"),
																 new OpSummonWisp(false));
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqaweewaqawee", HexDir.NORTH_WEST),
																 modLoc("wisp/summon/ticking"),
																 new OpSummonWisp(true));
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqaweewaqaweedw", HexDir.NORTH_WEST),
																 modLoc("wisp/media"),
																 OpWispMedia.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("aweewaqaweewaawww", HexDir.SOUTH_EAST),
																 modLoc("wisp/hex"),
																 OpWispHex.INSTANCE);
			
			// Set and Get Move Target WEST awqwawqaw
			PatternRegistry.mapPattern(HexPattern.fromAngles("awqwawqaw", HexDir.WEST),
																 modLoc("wisp/move_target/set"),
																 OpSetMoveTarget.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("ewdwewdew", HexDir.EAST),
																 modLoc("wisp/move_target/get"),
																 OpGetMoveTarget.INSTANCE);
			
			// Entity purification and Zone distillations
			PatternRegistry.mapPattern(HexPattern.fromAngles("qqwdedwqqdaqaaww", HexDir.SOUTH_EAST),
																 modLoc("get_entity/wisp"),
																 new OpGetEntityAt(entity -> entity instanceof BaseWisp));
			PatternRegistry.mapPattern(HexPattern.fromAngles("qqwdedwqqwdeddww", HexDir.SOUTH_EAST),
																 modLoc("zone_entity/wisp"),
																 new OpGetEntitiesBy(entity -> entity instanceof BaseWisp, false));
			PatternRegistry.mapPattern(HexPattern.fromAngles("eewaqaweewaqaaww", HexDir.NORTH_EAST),
																 modLoc("zone_entity/not_wisp"),
																 new OpGetEntitiesBy(entity -> entity instanceof BaseWisp, true));
			
			// Triggers
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqawded", HexDir.NORTH_WEST),
																 modLoc("wisp/trigger/tick"),
																 new OpWispSetTrigger(WispTriggerTypes.TICK_TRIGGER_TYPE));
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqqqqqwdeddw", HexDir.EAST),
																 modLoc("wisp/trigger/comm"),
																 new OpWispSetTrigger(WispTriggerTypes.COMM_TRIGGER_TYPE));
			PatternRegistry.mapPattern(HexPattern.fromAngles("eqwawqwaqww", HexDir.EAST),
																 modLoc("wisp/trigger/move"),
																 new OpWispSetTrigger(WispTriggerTypes.MOVE_TRIGGER_TYPE));
			
			// Great
			PatternRegistry.mapPattern(HexPattern.fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
																 modLoc("wisp/consume"),
																 OpConsumeWisp.INSTANCE, true);
			
			
			// =============================== Link Stuff =====================================
			
			PatternRegistry.mapPattern(HexPattern.fromAngles("eaqaaeqqqqqaweaqaaw", HexDir.EAST),
																 modLoc("link/link_entity"),
																 OpLinkEntity.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("eqqqqqawqeeeeedww", HexDir.EAST),
																 modLoc("link/link_two_entities"),
																 OpLinkEntities.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qdeddqeeeeedwqdeddw", HexDir.WEST),
																 modLoc("link/unlink"),
																 OpUnlink.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("eqqqqqaww", HexDir.EAST),
																 modLoc("link/get"),
																 OpGetLinked.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("aeqqqqqawwd", HexDir.SOUTH_WEST),
																 modLoc("link/get_index"),
																 OpGetLinkedIndex.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qeeeeedww", HexDir.WEST),
																 modLoc("link/num"),
																 OpNumLinked.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwdeddw", HexDir.NORTH_WEST),
																 modLoc("link/comm/send"),
																 OpSendIota.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("weeeeew", HexDir.NORTH_EAST),
																 modLoc("link/comm/read"),
																 OpReadReceivedIota.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("aweeeeewaa", HexDir.SOUTH_EAST),
																 modLoc("link/comm/num"),
																 OpNumReceivedIota.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("qwdedwq", HexDir.WEST),
																 modLoc("link/comm/open_transmit"),
																 OpOpenTransmit.INSTANCE);
			PatternRegistry.mapPattern(OpCloseTransmit.PATTERN,
																 modLoc("link/comm/close_transmit"),
																 OpCloseTransmit.INSTANCE);
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
