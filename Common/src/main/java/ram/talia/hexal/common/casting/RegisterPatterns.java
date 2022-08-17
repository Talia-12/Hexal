package ram.talia.hexal.common.casting;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import ram.talia.hexal.common.casting.actions.*;
import ram.talia.hexal.common.casting.actions.spells.OpSmelt;
import ram.talia.hexal.common.casting.actions.spells.OpSummonLemma;
import ram.talia.hexal.common.casting.actions.spells.great.OpConsumeLemma;

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
			
			// ============================== Misc Spells =====================================
			
			PatternRegistry.mapPattern(HexPattern.fromAngles("wqqqwqqadad", HexDir.EAST),
																 modLoc("smelt"),
																 OpSmelt.INSTANCE);
			
			// ============================== Lemma Stuff =====================================

			PatternRegistry.mapPattern(HexPattern.fromAngles("aqaeqeeeee", HexDir.NORTH_WEST),
																 modLoc("lemma/summon/projectile"),
																 new OpSummonLemma(false));
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqaweewaqawee", HexDir.NORTH_WEST),
																 modLoc("lemma/summon/ticking"),
																 new OpSummonLemma(true));
			PatternRegistry.mapPattern(HexPattern.fromAngles("aqaweewaqaweedw", HexDir.NORTH_WEST),
																 modLoc("lemma/media"),
																 OpLemmaMedia.INSTANCE);
			PatternRegistry.mapPattern(HexPattern.fromAngles("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
																 modLoc("lemma/consume"),
																 OpConsumeLemma.INSTANCE, true);
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
