package ram.talia.hexal.forge.eventhandlers;

import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.spell.math.HexPattern;
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
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.common.network.MsgRemoveEverbookAck;
import ram.talia.hexal.common.network.MsgSendEverbookSyn;
import ram.talia.hexal.common.network.MsgSetEverbookAck;
import ram.talia.hexal.forge.network.ForgePacketHandler;
import ram.talia.hexal.xplat.IClientXplatAbstractions;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EverbookEventHandler {
	private static final Map<UUID, Everbook> everbooks = new HashMap<>();
	
	/**
	 * This is the Everbook of the local client, null on the server.
	 */
	public static Everbook localEverbook;
	
	private static boolean syncedLocalToServer = false;
	
	public static Everbook getEverbook(Player player) {
		return everbooks.get(player.getUUID());
	}
	
	public static void setEverbook(Player player, Everbook everbook) {
		everbooks.put(player.getUUID(), everbook);
	}
	
	public static SpellDatum<?> getIota(ServerPlayer player, HexPattern key) {
		if (everbooks.get(player.getUUID()) == null)
			return SpellDatum.make(Widget.NULL);
		return everbooks.get(player.getUUID()).getIota(key, player.getLevel());
	}
	
	public static void setIota (ServerPlayer player, HexPattern key, SpellDatum<?> iota) {
		if (everbooks.get(player.getUUID()) == null)
			return;
		everbooks.get(player.getUUID()).setIota(key, iota);
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgSetEverbookAck(key, iota.serializeToNBT()));
	}
	
	public static void removeIota (ServerPlayer player, HexPattern key) {
		if (everbooks.get(player.getUUID()) == null)
			return;
		everbooks.get(player.getUUID()).removeIota(key);
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgRemoveEverbookAck(key));
	}
	
	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		if (!everbooks.containsKey(player.getUUID()))
			everbooks.put(player.getUUID(), new Everbook(player.getUUID()));
	}
	
	/**
	 * This is a PlayerTickEvent rather than a LoggedInEvent since *apparently* forge's network structure isn't set up at that point.
	 */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!syncedLocalToServer)
			HexalAPI.LOGGER.info("clientPlayerTick event " + event);
		
		if (event.player == null || event.side == LogicalSide.SERVER || syncedLocalToServer)
			return;
		
		syncedLocalToServer = true;
		
		localEverbook = Everbook.fromDisk(event.player.getUUID());
		// Doesn't work for SOME REASON with IClientXplatAbstractions.INSTANCE.sendPacketToServer
		ForgePacketHandler.getNetwork().sendToServer(new MsgSendEverbookSyn(localEverbook));
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		if (localEverbook != null)
			localEverbook.saveToDisk();
		
		// without this the localEverbook and sycnedLocalToServer keep their state when you leave the world and then rejoin which really screws things up.
		localEverbook = null;
		syncedLocalToServer = false;
	}
}
