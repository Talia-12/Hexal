package ram.talia.hexal.forge.eventhandlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.spell.casting.WispCastingManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages a Map of player UUIDs to {@link WispCastingManager}s,
 */
//@Mod.EventBusSubscriber(modid = "hexal", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class WispCastingMangerEventHandler {
	private static final String TAG_CASTING_MANAGER = "casting_manager";

	private static final Map<UUID, WispCastingManager> castingManagers = new HashMap<>();
	
	public static WispCastingManager getCastingManager(UUID uuid) {
		return castingManagers.get(uuid);
	}
	
	public static WispCastingManager getCastingManager(ServerPlayer serverPlayer) {
		return getCastingManager(serverPlayer.getUUID());
	}
	
	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer().getLevel().isClientSide())
			return;
		
		ServerPlayer player = (ServerPlayer) event.getPlayer();

		WispCastingManager manager = new WispCastingManager(player);
		manager.readFromNbt(player.getPersistentData().getCompound(TAG_CASTING_MANAGER), player.getLevel());
		castingManagers.put(player.getUUID(), manager);
	}
	
	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getPlayer().getLevel().isClientSide())
			return;
		
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		CompoundTag tag = new CompoundTag();
		getCastingManager(player).writeToNbt(tag);
		
		player.getPersistentData().put(TAG_CASTING_MANAGER, tag);
		
		castingManagers.remove(player.getUUID());
	}
	
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.side == LogicalSide.CLIENT)
			return;
		
		ServerPlayer player = (ServerPlayer) event.player;
		
		getCastingManager(player).executeCasts();
	}
}
