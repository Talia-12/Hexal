package ram.talia.hexal.forge.eventhandlers;

import net.minecraft.world.entity.player.Player;
import ram.talia.hexal.api.spell.casting.MacroHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MacroHolderEventHandler {
	private static final Map<UUID, MacroHolder> macroHolders = new HashMap<>();
	
	public static MacroHolder getMacroHolder(Player player) {
		return macroHolders.get(player.getUUID());
	}
	
	public static void setMacroHolder(Player player, MacroHolder macroHolder) { macroHolders.put(player.getUUID(), macroHolder); }
}
