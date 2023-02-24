package ram.talia.hexal.forge.eventhandlers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages a Map of player UUIDs to UUIDs for MediafiedStorage.
 */
public class BoundStorageEventHandler {
    private static final String TAG_BOUND_STORAGE = "hexal:bound_storage";

    // player UUIDs to MediafiedStorage UUIDs.
    private static final Map<UUID, UUID> boundStorage = new HashMap<>();

    public static @Nullable UUID getBoundStorage(ServerPlayer player) {
        return boundStorage.get(player.getUUID());
    }

    public static void setBoundStorage(ServerPlayer player, @Nullable UUID storage) {
        boundStorage.put(player.getUUID(), storage);
    }

    /**
     * Loads the saved bound storage UUID for each player if it exists.
     */
    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (player.getPersistentData().contains(TAG_BOUND_STORAGE))
            boundStorage.put(player.getUUID(), player.getPersistentData().getUUID(TAG_BOUND_STORAGE));
    }

    /**
     * Save each player's bound storage.
     */
    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (boundStorage.containsKey(player.getUUID()))
            player.getPersistentData().putUUID(TAG_BOUND_STORAGE, boundStorage.get(player.getUUID()));

        boundStorage.remove(player.getUUID());
    }
}
