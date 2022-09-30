package ram.talia.hexal.forge.eventhandlers;

import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.common.network.MsgRemoveEverbookAck;
import ram.talia.hexal.common.network.MsgSendEverbookSyn;
import ram.talia.hexal.common.network.MsgSetEverbookAck;
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
	
	public static Everbook getEverbook(Player player) {
		return everbooks.get(player.getUUID());
	}
	
	public static void setEverbook(Player player, Everbook everbook) {
		everbooks.put(player.getUUID(), everbook);
	}
	
	public static SpellDatum<?> getIota(ServerPlayer player, HexPattern key) {
		return everbooks.get(player.getUUID()).getIota(key, player.getLevel());
	}
	
	public static void setIota (ServerPlayer player, HexPattern key, SpellDatum<?> iota) {
		everbooks.get(player.getUUID()).setIota(key, iota);
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgSetEverbookAck(key, iota.serializeToNBT()));
	}
	
	public static void removeIota (ServerPlayer player, HexPattern key) {
		everbooks.get(player.getUUID()).removeIota(key);
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(player, new MsgRemoveEverbookAck(key));
	}
	
	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		if (!everbooks.containsKey(player.getUUID()))
			everbooks.put(player.getUUID(), new Everbook(player.getUUID()));
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
		Player player = event.getPlayer();
		
		if (player == null)
			return;
		
		localEverbook = Everbook.fromDisk(player.getUUID());
		IClientXplatAbstractions.INSTANCE.sendPacketToServer(new MsgSendEverbookSyn(localEverbook));
	}
}
