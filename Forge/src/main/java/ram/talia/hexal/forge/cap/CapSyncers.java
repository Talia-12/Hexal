package ram.talia.hexal.forge.cap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.LinkableRegistry;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.api.spell.HexalNBTHelperKt;
import ram.talia.hexal.forge.network.MsgPlayerAddRenderLinkAck;
import ram.talia.hexal.forge.network.MsgPlayerClearRenderLinksAck;
import ram.talia.hexal.forge.network.MsgPlayerRemoveRenderLinkAck;
import ram.talia.hexal.forge.network.MsgPlayerRenderLinksAck;
import ram.talia.hexal.xplat.IXplatAbstractions;

public class CapSyncers {
	@SubscribeEvent
	public static void syncDataOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (!(evt.getPlayer() instanceof ServerPlayer loggedInPlayer)) {
			return;
		}
		
		var allPlayers = loggedInPlayer.level.players();
		
		for (var player : allPlayers) {
			syncAllRenderLinks(loggedInPlayer, (ServerPlayer) player);
			syncAllRenderLinks((ServerPlayer) player, loggedInPlayer);
		}
	}
	
	@SubscribeEvent
	public static void syncDataOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
		if (!(evt.getPlayer() instanceof ServerPlayer loggedInPlayer)) {
			return;
		}
		
		var allPlayers = loggedInPlayer.level.players();
		
		for (var player : allPlayers) {
			syncAllRenderLinks(loggedInPlayer, (ServerPlayer) player);
			syncAllRenderLinks((ServerPlayer) player, loggedInPlayer);
		}
	}
	
	public static void syncAllRenderLinks (ServerPlayer packetTarget, ServerPlayer syncedPlayer) {
		PlayerLinkstore linkstore = IXplatAbstractions.INSTANCE.getLinkstore(syncedPlayer);
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(
						packetTarget,
						new MsgPlayerRenderLinksAck(syncedPlayer.getUUID(), HexalNBTHelperKt.toSyncTagILinkable(linkstore.getRenderLinks()))
		);
	}
	
	public static void syncAddRenderLink (ServerPlayer packetTarget, ServerPlayer syncedPlayer, ILinkable<?> link) {
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(packetTarget, new MsgPlayerAddRenderLinkAck(syncedPlayer.getUUID(), LinkableRegistry.wrapSync(link)));
	}
	
	public static void syncRemoveRenderLink (ServerPlayer packetTarget, ServerPlayer syncedPlayer, ILinkable<?> link) {
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(packetTarget, new MsgPlayerRemoveRenderLinkAck(syncedPlayer.getUUID(), LinkableRegistry.wrapSync(link)));
	}
	
	public static void syncClearRenderLinks (ServerPlayer packetTarget, ServerPlayer syncedPlayer) {
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(packetTarget, new MsgPlayerClearRenderLinksAck(syncedPlayer.getUUID()));
	}
}
