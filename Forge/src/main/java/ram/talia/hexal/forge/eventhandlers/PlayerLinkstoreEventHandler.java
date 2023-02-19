package ram.talia.hexal.forge.eventhandlers;

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
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Manages a Map of player UUIDs to {@link PlayerLinkstore}s,
 */
public class PlayerLinkstoreEventHandler {
	private static final String TAG_PLAYER_LINKSTORE = "hexal:player_linkstore";
	
	private static final Map<UUID, PlayerLinkstore> linkstores = new HashMap<>();

	// can't mark this as OnlyIn client, pretty sure it's since the = new HashMap<>() still tries to run regardless, and
	// breaks horribly.
	private static final Map<UUID, PlayerLinkstore.RenderCentre> renderCentres = new HashMap<>();
	
	public static @Nullable PlayerLinkstore.RenderCentre getRenderCentre(Player player) {
		return getRenderCentre(player.getUUID());
	}
	
	public static @Nullable PlayerLinkstore.RenderCentre getRenderCentre(UUID player) {
		return renderCentres.get(player);
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
	
	public static ILinkable getTransmittingTo(ServerPlayer player) {
		return getLinkstore(player).getTransmittingTo();
	}
	
	public static void setTransmittingTo(ServerPlayer player, int to) {
		getLinkstore(player).setTransmittingTo(to);
	}
	
	public static void resetTransmittingTo(ServerPlayer player) {
		getLinkstore(player).resetTransmittingTo();
	}
	
	/**
	 * Creates a {@link PlayerLinkstore} for each player, and loads saved data for said linkstore if saved data exists.
	 */
	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		linkstores.put(player.getUUID(), loadLinkstore(player));
	}
	
	@OnlyIn(Dist.CLIENT) // TODO: Remove apparently? https://docs.minecraftforge.net/en/latest/concepts/sides/#fmlenvironmentdist-and-onlyin
	@SubscribeEvent
	public static void clientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
		renderCentres.computeIfAbsent(event.getPlayer().getUUID(), k -> new PlayerLinkstore.RenderCentre(event.getPlayer()));
	}
	
	/**
	 * Save each player's {@link PlayerLinkstore} so links are saved when players log out.
	 */
	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		CompoundTag tag = new CompoundTag();
		getLinkstore(player).saveAdditionalData(tag);
		
		player.getPersistentData().put(TAG_PLAYER_LINKSTORE, tag);
		
		linkstores.remove(player.getUUID());
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
		if (event.getPlayer() == null)
			return;
		
		renderCentres.remove(event.getPlayer().getUUID());
	}
	
	/**
	 * Ticks each player's {@link PlayerLinkstore}, server to clean up removed links, client to render.
	 */
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) throws Exception {
		if (event.side == LogicalSide.CLIENT)
			clientTick(event.player);
		else
			serverTick((ServerPlayer) event.player);
	}
	
	private static void serverTick(ServerPlayer player) {
		linkstores.get(player.getUUID()).checkLinks();
	}
	
	@OnlyIn(Dist.CLIENT)
	private static void clientTick(Player player) throws Exception {
		if (!player.level.isClientSide)
			throw new Exception("PlayerLinkstoreEventHander.clientTick can only be called on the client"); // TODO
		
		// TODO: check if other players links on a server actually render, not sure if there is only a client player tick for your player.
		
		final var renderCentre = getRenderCentre(player);
		if (renderCentre != null)
			renderCentre.renderLinks();
	}
}
