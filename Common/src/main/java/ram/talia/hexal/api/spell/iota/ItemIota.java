package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.common.lib.HexalIotaTypes;

/**
 * Similar to GateIotas, stores a reference to an item stored in the
 * media. When the item is used up, all references to it become null.
 */
public class ItemIota extends Iota {
    static final String TAG_INDEX = "index";
    static final String TAG_DISPLAY_NAME = "name";
    static final String TAG_COUNT = "count";

    public ItemIota(int payload) {
        super(HexalIotaTypes.ITEM, payload);
    }

    public int getItemIndex() {
        return (int) payload;
    }

    public Item getItem() {
        return MediafiedItemManager.getItem(this.getItemIndex());
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) &&
                that instanceof ItemIota ithat &&
                this.getItemIndex() == ithat.getItemIndex();
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public @NotNull Tag serialize() {
        // needs to contain both the index and the current contents
        // of the referenced item, since the same serialised is
        // used for both storage and for sending to the client
        // to display.

        var tag = new CompoundTag();
        tag.putInt(TAG_INDEX, this.getItemIndex());

        var record = MediafiedItemManager.getRecord(this.getItemIndex());

        MediafiedItemManager.ItemRecord rec;

        if (record == null || (rec = record.get()) == null)
            return tag;


        tag.putString(TAG_DISPLAY_NAME, rec.getDisplayName().getString());
        tag.putInt(TAG_COUNT, rec.getCount());

        return tag;
    }

    public static IotaType<ItemIota> TYPE = new IotaType<>() {
        @Override
        public ItemIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var ctag = HexUtils.downcast(tag, CompoundTag.TYPE);

            int index = ctag.getInt(TAG_INDEX);

            if (!MediafiedItemManager.contains(index))
                return null;

            return new ItemIota(index);
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof CompoundTag ctag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }

            return Component.translatable("hexal.spelldata.item", ctag.getString(TAG_DISPLAY_NAME), ctag.getInt(TAG_COUNT)).withStyle(ChatFormatting.AQUA);
        }

        @Override
        public int color() {
            return 0xff_55ffff;
        }
    };
}
