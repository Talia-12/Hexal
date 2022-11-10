package ram.talia.hexal.common.casting;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.Action;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import kotlin.Triple;
import net.minecraft.resources.ResourceLocation;

public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			for (Triple<HexPattern, ResourceLocation, Action> triple : Patterns.PATTERNS) {
				PatternRegistry.mapPattern(triple.getFirst(), triple.getSecond(), triple.getThird());
			}
			for (Triple<HexPattern, ResourceLocation, Action> triple : Patterns.PER_WORLD_PATTERNS) {
				PatternRegistry.mapPattern(triple.getFirst(), triple.getSecond(), triple.getThird(), true);
			}
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
