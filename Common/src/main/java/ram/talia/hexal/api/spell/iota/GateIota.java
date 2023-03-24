package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.gates.GateManager;
import ram.talia.hexal.common.lib.HexalIotaTypes;

import java.util.HashSet;
import java.util.Set;

public class GateIota extends Iota {

    public GateIota(int payload) {
        super(HexalIotaTypes.GATE, payload);
    }

    public int getGateIndex() {
        return (int) this.payload;
    }

    public Set<Entity> getMarked(ServerLevel level) {
        var marked = GateManager.allMarked.getOrDefault(this.getGateIndex(), new HashSet<>());

        var out = new HashSet<Entity>();
        for (var mark : marked) {
            var markEntity = level.getEntity(mark);
            if (markEntity != null)
                out.add(markEntity);
        }
        return out;
    }

    public boolean isMarked(Entity entity) {
        var marked = GateManager.allMarked.getOrDefault(this.getGateIndex(), new HashSet<>());
        return marked.contains(entity.getUUID());
    }

    public int getNumMarked() {
        return GateManager.allMarked.getOrDefault(this.getGateIndex(), new HashSet<>()).size();
    }

    public void mark(Entity entity) {
        GateManager.mark(this.getGateIndex(), entity);
    }

    public void unmark(@NotNull Entity entity) {
        GateManager.unmark(this.getGateIndex(), entity);
    }

    public void clearMarked() {
        GateManager.clearMarked(this.getGateIndex());
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) &&
                that instanceof GateIota gthat &&
                this.getGateIndex() == gthat.getGateIndex();
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public @NotNull Tag serialize() {
        return IntTag.valueOf(this.getGateIndex());
    }

    public static IotaType<GateIota> TYPE = new IotaType<>() {
        @Override
        public GateIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var itag = HexUtils.downcast(tag, IntTag.TYPE);

            return new GateIota(itag.getAsInt());
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof IntTag itag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }

            return Component.translatable("hexal.spelldata.gate", itag.getAsInt()).withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        @Override
        public int color() {
            return 0xff_ff55ff;
        }
    };
}
