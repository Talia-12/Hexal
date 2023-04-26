package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import com.mojang.datafixers.util.Either;
import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.gates.GateManager;
import ram.talia.hexal.common.lib.HexalIotaTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GateIota extends Iota {

    public static String TAG_INDEX = "index";
    public static String TAG_TARGET_TYPE = "target_type";
    public static String TAG_TARGET_X = "target_x";
    public static String TAG_TARGET_Y = "target_y";
    public static String TAG_TARGET_Z = "target_z";
    public static String TAG_TARGET_UUID = "target_uuid";

    private record Payload(int index, Either<Vec3, Pair<UUID, Vec3>> target) { }

    public GateIota(int index, @Nullable Either<Vec3, Pair<Entity, Vec3>> target) {
        super(HexalIotaTypes.GATE, new Payload(index, target == null ? null : target.mapRight(pair -> new Pair<>(pair.getFirst().getUUID(), pair.getSecond()))));
    }

    public GateIota(Payload payload) {
        super(HexalIotaTypes.GATE, payload);
    }

    public int getGateIndex() {
        return ((Payload) this.payload).index;
    }

    public @Nullable Either<Vec3, Pair<UUID, Vec3>> getTarget() {
        return ((Payload) this.payload).target;
    }

    public @Nullable Vec3 getTargetPos(ServerLevel level) {
        var target = this.getTarget();

        if (target == null)
            return null;

        return target.map(vec3 -> vec3, pair -> {
            var entity = level.getEntity(pair.component1());

            if (entity == null)
                return null;

            return entity.position().add(pair.component2());
        });
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
        var tag = new CompoundTag();

        tag.putInt(TAG_INDEX, this.getGateIndex());

        if (this.getTarget() == null)
            tag.putByte(TAG_TARGET_TYPE, (byte) 0);
        else {
            tag.putByte(TAG_TARGET_TYPE, this.getTarget().map(vec3 -> 1, pair -> 2).byteValue());
            tag.putDouble(TAG_TARGET_X, this.getTarget().map(vec3 -> vec3.x, pair -> pair.getSecond().x));
            tag.putDouble(TAG_TARGET_Y, this.getTarget().map(vec3 -> vec3.y, pair -> pair.getSecond().y));
            tag.putDouble(TAG_TARGET_Z, this.getTarget().map(vec3 -> vec3.z, pair -> pair.getSecond().z));

            this.getTarget().ifRight(pair -> tag.putUUID(TAG_TARGET_UUID, pair.getFirst()));
        }

        return IntTag.valueOf(this.getGateIndex());
    }

    public static IotaType<GateIota> TYPE = new IotaType<>() {
        @Override
        public GateIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var ctag = HexUtils.downcast(tag, CompoundTag.TYPE);

            var index = ctag.getInt(TAG_INDEX);
            var type = ctag.getByte(TAG_TARGET_TYPE);

            if (type == 0) { // Drifting Gate
                return new GateIota(index, null);
            }

            var x = ctag.getDouble(TAG_TARGET_X);
            var y = ctag.getDouble(TAG_TARGET_Y);
            var z = ctag.getDouble(TAG_TARGET_Z);
            var vec = new Vec3(x, y, z);

            if (type == 1) { // Location Bound Gate
                return new GateIota(index, Either.left(vec));
            }

            var uuid = ctag.getUUID(TAG_TARGET_UUID);

            return new GateIota(new Payload(index, Either.right(new Pair<>(uuid, vec))));
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
