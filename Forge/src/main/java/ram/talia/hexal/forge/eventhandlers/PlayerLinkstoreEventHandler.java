package ram.talia.hexal.forge.eventhandlers;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.client.RenderHelperKt;
import ram.talia.hexal.forge.cap.CapSyncers;

import java.util.*;

/**
 * Manages a Map of player UUIDs to {@link PlayerLinkstore}s,
 */
public class PlayerLinkstoreEventHandler {
	private static final String TAG_PLAYER_LINKSTORE = "player_linkstore";
	
	private static final Map<UUID, PlayerLinkstore> linkstores = new HashMap<>();
	private static final Map<UUID, List<ILinkable.IRenderCentre>> renderLinks = new HashMap<>();
	
	public static List<ILinkable.IRenderCentre> getRenderLinks(Player player) {
		return getRenderLinks(player.getUUID());
	}
	
	public static List<ILinkable.IRenderCentre> getRenderLinks(UUID player) {
		return renderLinks.get(player);
	}
	
	public static List<ILinkable.IRenderCentre> setRenderLinks(Player player, List<ILinkable.IRenderCentre> newRenderLinks) {
		return setRenderLinks(player.getUUID(), newRenderLinks);
	}
	
	public static List<ILinkable.IRenderCentre> setRenderLinks(UUID player, List<ILinkable.IRenderCentre> newRenderLinks) {
		return renderLinks.put(player, newRenderLinks);
	}
	
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
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		linkstores.put(player.getUUID(), loadLinkstore(player));
	}
	
	@SubscribeEvent
	public static void clientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
		if (event.getPlayer() == null)
			return;
		
		renderLinks.computeIfAbsent(event.getPlayer().getUUID(), k -> new ArrayList<>());
	}
	
	/**
	 * Save each player's {@link PlayerLinkstore} so links are saved when players log out.
	 */
	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		CompoundTag tag = new CompoundTag();
		getLinkstore(player).saveAdditionalData(tag);
		
		player.getPersistentData().put(TAG_PLAYER_LINKSTORE, tag);
		
		linkstores.remove(player.getUUID());
	}
	
	@SubscribeEvent
	public static void clientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		if (event.getPlayer() == null)
			return;
		
		renderLinks.remove(event.getPlayer().getUUID());
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
		var ownerRenderCentre = new PlayerLinkstore.RenderCentre(player);
		var theseLinks = getRenderLinks(player);
		if (theseLinks == null)
			return;
		for (var link : theseLinks) {
			RenderHelperKt.playLinkParticles(ownerRenderCentre, link, IXplatAbstractions.INSTANCE.getColorizer(player), player.getRandom(), player.level);
		}
	}
}
