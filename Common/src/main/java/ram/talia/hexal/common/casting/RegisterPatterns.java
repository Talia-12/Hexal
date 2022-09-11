package ram.talia.hexal.common.casting;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import ram.talia.hexal.common.casting.actions.*;
import ram.talia.hexal.common.casting.actions.spells.OpFallingBlock;
import ram.talia.hexal.common.casting.actions.spells.OpFreeze;
import ram.talia.hexal.common.casting.actions.spells.OpSmelt;
import ram.talia.hexal.common.casting.actions.spells.link.*;
import ram.talia.hexal.common.casting.actions.spells.wisp.*;
import ram.talia.hexal.common.casting.actions.spells.great.OpConsumeWisp;

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
			PatternRegistry.mapPattern(HexPattern.fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
																 modLoc("wisp/consume"),
																 OpConsumeWisp.INSTANCE, true);
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
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
