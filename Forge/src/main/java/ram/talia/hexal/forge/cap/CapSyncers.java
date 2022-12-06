package ram.talia.hexal.forge.cap;

import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.LinkableRegistry;
import ram.talia.hexal.common.network.MsgAddRenderLinkAck;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkAck;
import ram.talia.hexal.common.network.MsgSetRenderLinksAck;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.List;

public class CapSyncers {
	@SubscribeEvent
	public static void syncDataOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (!(evt.getEntity() instanceof ServerPlayer loggedInPlayer)) {
			return;
		}
		
		var allPlayers = loggedInPlayer.level.players();
		
		for (var player : allPlayers) {
			syncAllRenderLinks((ServerPlayer) player, loggedInPlayer);
			if (player.getUUID() != loggedInPlayer.getUUID())
				syncAllRenderLinks(loggedInPlayer, (ServerPlayer) player);
		}
	}
	
	@SubscribeEvent
	public static void syncDataOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
		if (!(evt.getEntity() instanceof ServerPlayer loggedInPlayer)) {
			return;
		}
		
		var allPlayers = loggedInPlayer.level.players();
		
		for (var player : allPlayers) {
			syncAllRenderLinks(loggedInPlayer, (ServerPlayer) player);
			if (player.getUUID() != loggedInPlayer.getUUID())
				syncAllRenderLinks((ServerPlayer) player, loggedInPlayer);
		}
	}

	private static void syncAllRenderLinks(ServerPlayer loggedInPlayer, ServerPlayer player) {
		HexalAPI.LOGGER.info("TODO: Re-setup syncing of player render links."); //TODO
	}

	public static void syncAddRenderLink (ServerPlayer packetTarget, ILinkable thisLink, ILinkable otherLink) {
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(packetTarget,
				new MsgAddRenderLinkAck(LinkableRegistry.wrapSync(thisLink), LinkableRegistry.wrapSync(otherLink)));
	}
	
	public static void syncRemoveRenderLink (ServerPlayer packetTarget, ILinkable thisLink, ILinkable otherLink) {
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(packetTarget,
				new MsgRemoveRenderLinkAck(LinkableRegistry.wrapSync(thisLink), LinkableRegistry.wrapSync(otherLink)));
	}

	private static ListTag getSyncTag(List<ILinkable> others) {
		ListTag listTag = new ListTag();
		others.forEach(it -> listTag.add(LinkableRegistry.wrapSync(it)));
		return listTag;
	}

	public static void syncSetRenderLinks (ServerPlayer packetTarget, ILinkable thisLink, List<ILinkable> others) {
		IXplatAbstractions.INSTANCE.sendPacketToPlayer(packetTarget,
				new MsgSetRenderLinksAck(LinkableRegistry.wrapSync(thisLink), getSyncTag(others)));
	}
}
