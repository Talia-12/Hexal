package ram.talia.hexal.forge.eventhandlers;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.forge.saveddata.GateSavedData;

/**
 * Responsible for saving and loading the [GateManager] data.
 */
public class GateEventHandler {
    static final String FILE_GATE_MANAGER = "hexal_gate_manager";

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        var savedData = event.getServer().overworld().getDataStorage().computeIfAbsent(GateSavedData::new, GateSavedData::new, FILE_GATE_MANAGER);
        savedData.setDirty();
    }
}
