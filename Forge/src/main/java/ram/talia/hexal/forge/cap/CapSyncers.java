package ram.talia.hexal.forge.cap;

import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.LinkableRegistry;
import ram.talia.hexal.common.network.MsgAddRenderLinkAck;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkAck;
import ram.talia.hexal.common.network.MsgSetRenderLinksAck;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.List;

public class CapSyncers {
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
