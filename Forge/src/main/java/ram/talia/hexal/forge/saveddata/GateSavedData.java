package ram.talia.hexal.forge.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.gates.GateManager;

public class GateSavedData extends SavedData {
    public GateSavedData() {  }

    public GateSavedData(CompoundTag tag) {
        GateManager.readFromNbt(tag);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        GateManager.writeToNbt(tag);

        return tag;
    }


}
