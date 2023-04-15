package ram.talia.hexal.forge.eventhandlers;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.api.gates.GateManager;
import ram.talia.hexal.api.gates.GateSavedData;

/**
 * Responsible for saving and loading the [GateManager] data.
 */
public class GateEventHandler {
    static final String FILE_GATE_MANAGER = "hexal_gate_manager";

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var savedData = event.getServer().overworld().getDataStorage().computeIfAbsent(GateSavedData::new, GateSavedData::new, FILE_GATE_MANAGER);
        savedData.setDirty();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        var savedData = event.getServer().overworld().getDataStorage().computeIfAbsent(GateSavedData::new, GateSavedData::new, FILE_GATE_MANAGER);
        GateManager.shouldClearOnWrite = true;
        savedData.setDirty();
    }
}
