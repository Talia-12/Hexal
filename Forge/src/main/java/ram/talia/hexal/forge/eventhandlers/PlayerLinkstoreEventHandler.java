package ram.talia.hexal.forge.eventhandlers;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.lang3.ObjectUtils;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.api.spell.casting.WispCastingManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages a Map of player UUIDs to {@link PlayerLinkstore}s,
 */
public class PlayerLinkstoreEventHandler {
	private static final String TAG_PLAYER_LINKSTORE = "player_linkstore";
	
	private static final Map<UUID, PlayerLinkstore> linkstores = new HashMap<>();
	
	public static PlayerLinkstore getLinkstore(ServerPlayer player) {
		PlayerLinkstore linkstore = linkstores.get(player.getUUID());
		
		if (linkstore == null) {
			linkstore = loadLinkstore(player);
			linkstores.put(player.getUUID(), linkstore);
		}
		
		return linkstore;
	}
	
	private static PlayerLinkstore loadLinkstore(ServerPlayer player) {
		PlayerLinkstore linkstore = new PlayerLinkstore(player);
		linkstore.loadAdditionalData(player.getPersistentData().getCompound(TAG_PLAYER_LINKSTORE));
		return linkstore;
	}
	
	/**
	 * Creates a {@link PlayerLinkstore} for each player, and loads saved data for said linkstore if saved data exists.
	 */
	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer().getLevel().isClientSide())
			return;
		
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		linkstores.put(player.getUUID(), loadLinkstore(player));
	}
	
	/**
	 * Save each player's {@link PlayerLinkstore} so links are saved when players log out.
	 */
	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getPlayer().getLevel().isClientSide())
			return;
		
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		CompoundTag tag = new CompoundTag();
		getLinkstore(player).saveAdditionalData(tag);
		
		player.getPersistentData().put(TAG_PLAYER_LINKSTORE, tag);
		
		linkstores.remove(player.getUUID());
	}
	
	/**
	 * Ticks each player's {@link PlayerLinkstore}, server to clean up removed links, client to render.
	 */
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.side == LogicalSide.CLIENT)
			clientTick((AbstractClientPlayer) event.player);
		else
			serverTick((ServerPlayer) event.player);
	}
	
	private static void serverTick(ServerPlayer player) {
		linkstores.get(player.getUUID()).pruneLinks();
	}
	
	private static void clientTick(AbstractClientPlayer player) {
		//TODO: Render links
	}
}
