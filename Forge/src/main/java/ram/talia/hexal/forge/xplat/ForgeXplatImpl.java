package ram.talia.hexal.forge.xplat;

import net.minecraft.server.level.ServerPlayer;
import ram.talia.hexal.api.spell.casting.WispCastingManager;
import ram.talia.hexal.forge.eventhandlers.WispCastingMangerEventHandler;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.Optional;

public class ForgeXplatImpl implements IXplatAbstractions {
	@Override
	public Optional<WispCastingManager> getWispCastingManager (ServerPlayer caster) {
		return Optional.ofNullable(WispCastingMangerEventHandler.getCastingManager(caster));
	}
}
