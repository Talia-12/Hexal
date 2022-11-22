package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.lib.HexalIotaTypes;

public class IotaTypeIota extends Iota {
    public IotaTypeIota(@NotNull IotaType<?> iotaType) {
        super(HexalIotaTypes.IOTA_TYPE, iotaType);
    }

    public IotaType<?> getIotaType() {
        return (IotaType<?>) this.payload;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) &&
                that instanceof IotaTypeIota dent &&
                this.getIotaType().equals(dent.getIotaType());
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public @NotNull Tag serialize() {
        ResourceLocation location = HexIotaTypes.REGISTRY.getKey(this.getIotaType());
        if (location == null)
            return StringTag.valueOf("");
        return StringTag.valueOf(location.toString());
    }

    public static IotaType<IotaTypeIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public IotaTypeIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var stag = HexUtils.downcast(tag, StringTag.TYPE);

            var typeLocation = ResourceLocation.read(stag.getAsString());
            var type = typeLocation.map(HexIotaTypes.REGISTRY::get).get().left();

            return type.map(IotaTypeIota::new).orElse(null);
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof StringTag stag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }
            var typeLocation = ResourceLocation.read(stag.getAsString());
            var type = typeLocation.map(HexIotaTypes.REGISTRY::get).get().left();

            return type.map(t -> {
                var location = HexIotaTypes.REGISTRY.getKey(t);
                assert location != null;
                return Component.translatable("hexcasting.iota.%s".formatted(location.toString()))
                        .withStyle(ChatFormatting.DARK_PURPLE);
            }).orElse(Component.translatable("hexcasting.spelldata.unknown"));
        }

        @Override
        public int color() {
            return 0xff_553355;
        }
    };
}
