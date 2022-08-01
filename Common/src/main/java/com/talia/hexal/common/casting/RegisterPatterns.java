package com.talia.hexal.common.casting;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import com.talia.hexal.common.casting.actions.OpCompareBlocks;

import static com.talia.hexal.api.HexalAPI.modLoc;

public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			PatternRegistry.mapPattern(HexPattern.fromAngles("qaqqaea", HexDir.EAST),
									   modLoc("compare_blocks"),
									   OpCompareBlocks.INSTANCE);
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
