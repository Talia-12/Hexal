package ram.talia.hexal.forge.eventhandlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.casting.wisp.WispCastingManager;
import ram.talia.hexal.common.entities.BaseCastingWisp;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages a Map of player UUIDs to {@link WispCastingManager}s,
 */
//@Mod.EventBusSubscriber(modid = "hexal", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class WispCastingMangerEventHandler {
	private static final String TAG_CASTING_MANAGER = "hexal:casting_manager";
	private static final String TAG_SEON = "hexal:seon";

	private static final Map<UUID, WispCastingManager> castingManagers = new HashMap<>();
	private static final Map<UUID, UUID> seonUUIDs = new HashMap<>();
	private static final Map<UUID, WeakReference<BaseCastingWisp>> seons = new HashMap<>();

	public static WispCastingManager getCastingManager(ServerPlayer serverPlayer) {
		WispCastingManager manager = castingManagers.get(serverPlayer.getUUID());
		if (manager == null) {
			manager = loadCastingManager(serverPlayer);
			castingManagers.put(serverPlayer.getUUID(), manager);
		}
		return manager;
	}
	
	private static WispCastingManager loadCastingManager(ServerPlayer player) {
		WispCastingManager manager = new WispCastingManager(player);
		manager.readFromNbt(player.getPersistentData().getCompound(TAG_CASTING_MANAGER), player.getLevel());
		return manager;
	}

	public static @Nullable BaseCastingWisp getSeon(ServerPlayer player) {
		var ref = seons.get(player.getUUID());
		if (ref == null) {
			return refreshSeonFromLevel(player);
		}

		return ref.get();
	}

	public static void setSeon(ServerPlayer player, BaseCastingWisp wisp) {
		var oldRef = seons.get(player.getUUID());
		if (oldRef == null) {
			var oldSeon = refreshSeonFromLevel(player);
			if (oldSeon != null) {
				oldSeon.setSeon(false);
			}
		} else {
			var oldSeon = oldRef.get();
			if (oldSeon != null)
				oldSeon.setSeon(false);
		}
		wisp.setSeon(true);
		seonUUIDs.put(player.getUUID(), wisp.getUUID());
		seons.put(player.getUUID(), new WeakReference<>(wisp));
	}

	private static void loadSeon(ServerPlayer player) {
		if (!player.getPersistentData().hasUUID(TAG_SEON))
			return;

		var seonUUID = player.getPersistentData().getUUID(TAG_SEON);

		var entity = player.getLevel().getEntity(seonUUID);
		if (entity == null) {
			seonUUIDs.put(player.getUUID(), seonUUID);
			return;
		}

		if (entity instanceof BaseCastingWisp seon) {
			seonUUIDs.put(player.getUUID(), seonUUID);
			seons.put(player.getUUID(), new WeakReference<>(seon));
		}
	}

	private static @Nullable BaseCastingWisp refreshSeonFromLevel(ServerPlayer player) {
		var seonUUID = seonUUIDs.get(player.getUUID());
		if (seonUUID == null)
			return null;
		var entity = player.getLevel().getEntity(seonUUID);

		if (!(entity instanceof BaseCastingWisp seon))
			return null;

		seons.put(player.getUUID(), new WeakReference<>(seon));
		return seon;
	}

	/**
	 * Creates a {@link WispCastingManager} for each player, and loads saved data for said casting manager if saved data exists.
	 */
	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity().getLevel().isClientSide())
			return;
		
		ServerPlayer player = (ServerPlayer) event.getEntity();

		castingManagers.put(player.getUUID(), loadCastingManager(player));
		loadSeon(player);
	}
	
	/**
	 * Save each player's {@link WispCastingManager} so that casts which haven't resolved yet will
	 * resolve when the player logs back in.
	 */
	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity().getLevel().isClientSide())
			return;
		
		ServerPlayer player = (ServerPlayer) event.getEntity();
		
		CompoundTag tag = new CompoundTag();
		getCastingManager(player).writeToNbt(tag);
		
		player.getPersistentData().put(TAG_CASTING_MANAGER, tag);
		var seonUUID = seonUUIDs.get(player.getUUID());
		if (seonUUID != null) {
			player.getPersistentData().putUUID(TAG_SEON, seonUUID);
		}

		castingManagers.remove(player.getUUID());
		seonUUIDs.remove(player.getUUID());
		seons.remove(player.getUUID());
	}
	
	/**
	 * Ticks each player's {@link WispCastingManager}, meaning that their wisps casts execute properly.
	 */
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.side == LogicalSide.CLIENT)
			return;
		
		ServerPlayer player = (ServerPlayer) event.player;
		
		getCastingManager(player).executeCasts();
	}
}
