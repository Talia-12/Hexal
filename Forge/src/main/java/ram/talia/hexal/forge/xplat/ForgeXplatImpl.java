package ram.talia.hexal.forge.xplat;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.network.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.api.spell.casting.WispCastingManager;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.common.network.MsgAddRenderLinkAck;
import ram.talia.hexal.common.network.MsgRemoveRenderLinkAck;
import ram.talia.hexal.common.network.MsgSetRenderLinksAck;
import ram.talia.hexal.forge.eventhandlers.EverbookEventHandler;
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler;
import ram.talia.hexal.forge.eventhandlers.WispCastingMangerEventHandler;
import ram.talia.hexal.forge.network.ForgePacketHandler;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.List;

public class ForgeXplatImpl implements IXplatAbstractions {

	@Override
	public boolean isPhysicalClient() {
		return FMLLoader.getDist() == Dist.CLIENT;
	}

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
	public void sendPacketTracking(Entity entity, IMessage packet) {
		ForgePacketHandler.getNetwork().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
	}

	@Override
	public void sendPacketTracking(BlockEntity blockEntity, IMessage packet) {
		sendPacketTracking(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel(), packet);
	}

	@Override
	public void sendPacketTracking(BlockPos pos, ServerLevel dimension, IMessage packet) {
		ForgePacketHandler.getNetwork().send(PacketDistributor.TRACKING_CHUNK.with(() -> dimension.getChunkAt(pos)), packet);
	}

	@Override
	public void sendPacketTracking(ChunkPos pos, ServerLevel dimension, IMessage packet) {
		sendPacketTracking(pos.getWorldPosition(), dimension, packet);
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
	public void setSeon(ServerPlayer caster, BaseCastingWisp wisp) {
		WispCastingMangerEventHandler.setSeon(caster, wisp);
	}

	@Override
	public @Nullable BaseCastingWisp getSeon(ServerPlayer caster) {
		return WispCastingMangerEventHandler.getSeon(caster);
	}

	@Override
	public PlayerLinkstore getLinkstore (ServerPlayer player) {
		return PlayerLinkstoreEventHandler.getLinkstore(player);
	}

	@Override
	public PlayerLinkstore.RenderCentre getPlayerRenderCentre(Player player) {
		return PlayerLinkstoreEventHandler.getRenderCentre(player);
	}

	@Override
	public void syncAddRenderLink(ILinkable sourceLink, ILinkable sinkLink, ServerLevel level) {
		sendPacketTracking(new BlockPos(sourceLink.getPosition()), level, new MsgAddRenderLinkAck(sourceLink, sinkLink));
	}
	
	@Override
	public void syncRemoveRenderLink(ILinkable sourceLink, ILinkable sinkLink, ServerLevel level) {
		sendPacketTracking(new BlockPos(sourceLink.getPosition()), level, new MsgRemoveRenderLinkAck(sourceLink, sinkLink));
	}

	@Override
	public void syncSetRenderLinks(ILinkable sourceLink, List<ILinkable> sinks, ServerLevel level) {
		sendPacketTracking(new BlockPos(sourceLink.getPosition()), level, new MsgSetRenderLinksAck(sourceLink, sinks));
	}
	
	@Override
	public ILinkable getPlayerTransmittingTo (ServerPlayer player) {
		return PlayerLinkstoreEventHandler.getTransmittingTo(player);
	}
	
	@Override
	public void setPlayerTransmittingTo (ServerPlayer player, int to) {
		PlayerLinkstoreEventHandler.setTransmittingTo(player, to);
	}
	
	@Override
	public void resetPlayerTransmittingTo (ServerPlayer player) {
		PlayerLinkstoreEventHandler.resetTransmittingTo(player);
	}
	
	@Override
	public Iota getEverbookIota (ServerPlayer player, HexPattern key) {
		return EverbookEventHandler.getIota(player, key);
	}
	
	@Override
	public void setEverbookIota (ServerPlayer player, HexPattern key, Iota iota) {
		EverbookEventHandler.setIota(player, key, iota);
	}
	
	@Override
	public void removeEverbookIota (ServerPlayer player, HexPattern key) {
		EverbookEventHandler.removeIota(player, key);
	}
	
	@Override
	public void setFullEverbook (ServerPlayer player, Everbook everbook) {
		EverbookEventHandler.setEverbook(player, everbook);
	}
	
	@Override
	public List<Iota> getEverbookMacro (ServerPlayer player, HexPattern key) {
		return EverbookEventHandler.getMacro(player, key);
	}
	
	@Override
	public void toggleEverbookMacro (ServerPlayer player, HexPattern key) {
		EverbookEventHandler.toggleMacro(player, key);
	}
	
	@Override
	public boolean isBreakingAllowed (Level level, BlockPos pos, BlockState state, Player player) {
		return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player));
	}
}
