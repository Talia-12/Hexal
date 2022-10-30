package ram.talia.hexal.common.casting;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.Operator;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntitiesBy;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntityAt;
import com.mojang.datafixers.util.Pair;
import kotlin.Triple;
import net.minecraft.resources.ResourceLocation;
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

import java.util.ArrayList;
import java.util.List;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class RegisterPatterns {
	public static void registerPatterns () {
		try {
			for (Triple<HexPattern, ResourceLocation, Operator> triple : Patterns.PATTERNS) {
				PatternRegistry.mapPattern(triple.getFirst(), triple.getSecond(), triple.getThird());
			}
			for (Triple<HexPattern, ResourceLocation, Operator> triple : Patterns.PER_WORLD_PATTERNS) {
				PatternRegistry.mapPattern(triple.getFirst(), triple.getSecond(), triple.getThird(), true);
			}
		}
		catch (PatternRegistry.RegisterPatternException e) {
			e.printStackTrace();
		}
	}
}
