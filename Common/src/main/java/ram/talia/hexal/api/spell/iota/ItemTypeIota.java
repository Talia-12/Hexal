package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.lib.HexalIotaTypes;

public class ItemTypeIota extends Iota {
    public ItemTypeIota(@NotNull Item item) {
        super(HexalIotaTypes.ITEM_TYPE, item);
    }

    public Item getItem() {
        return (Item) this.payload;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public @NotNull Tag serialize() {
        return StringTag.valueOf(Registry.ITEM.getKey(this.getItem()).toString());
    }

    public static IotaType<ItemTypeIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public ItemTypeIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var stag = HexUtils.downcast(tag, StringTag.TYPE);

            var typeLocation = ResourceLocation.read(stag.getAsString());
            var type = typeLocation.map(Registry.ITEM::get).get().left();

            return type.map(ItemTypeIota::new).orElse(null);
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof StringTag stag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }
            var typeLocation = ResourceLocation.read(stag.getAsString());
            var type = typeLocation.map(Registry.ITEM::get).get().left();

            return type.map(t -> t.getDescription().copy().withStyle(ChatFormatting.GOLD))
                    .orElse(Component.translatable("hexcasting.spelldata.unknown"));
        }

        @Override
        public int color() {
            return 0xff_5555ff;
        }
    };
}
