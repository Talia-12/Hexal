package ram.talia.hexal.forge.xplat;

import at.petrak.hexcasting.common.network.IMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.api.spell.casting.WispCastingManager;
import ram.talia.hexal.forge.cap.CapSyncers;
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler;
import ram.talia.hexal.forge.eventhandlers.WispCastingMangerEventHandler;
import ram.talia.hexal.forge.network.ForgePacketHandler;
import ram.talia.hexal.xplat.IXplatAbstractions;

public class ForgeXplatImpl implements IXplatAbstractions {
	
	@Override
	public void sendPacketToPlayer(ServerPlayer target, IMessage packet) {
		ForgePacketHandler.getNetwork().send(PacketDistributor.PLAYER.with(() -> target), packet);
	}
	
	@Override
	public void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet) {
		ForgePacketHandler.getNetwork().send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
						pos.x, pos.y, pos.z, radius * radius, dimension.dimension()
		)), packet);
	}
	
	@Override
	public Packet<?> toVanillaClientboundPacket(IMessage message) {
		return ForgePacketHandler.getNetwork().toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT);
	}
	@Override
	public WispCastingManager getWispCastingManager (ServerPlayer caster) {
		return WispCastingMangerEventHandler.getCastingManager(caster);
	}
	
	@Override
	public PlayerLinkstore getLinkstore (ServerPlayer player) {
		return PlayerLinkstoreEventHandler.getLinkstore(player);
	}
	
	@Override
	public void syncAddRenderLinkPlayer (ServerPlayer player, ILinkable<?> link) {
		var allPlayers = player.level.players();
		
		for (var other : allPlayers) {
			CapSyncers.syncAddRenderLink((ServerPlayer) other, player, link);
		}
	}
	
	@Override
	public void syncRemoveRenderLinkPlayer (ServerPlayer player, ILinkable<?> link) {
		var allPlayers = player.level.players();
		
		for (var other : allPlayers) {
			CapSyncers.syncRemoveRenderLink((ServerPlayer) other, player, link);
		}
	}
}
