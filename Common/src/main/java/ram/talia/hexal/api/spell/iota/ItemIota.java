package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.common.lib.HexalIotaTypes;

import java.util.List;
import java.util.Objects;

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

    public ItemIota(ItemStack stack) {
        super(HexalIotaTypes.ITEM, MediafiedItemManager.assignItem(stack));
    }

    /**
     * Returns the ItemIota if its item still exists, or null otherwise. SHOULD ALWAYS
     * BE CALLED BEFORE MAKING USE OF AN ITEM IOTA (built into List<Iota>.getItem).
     */
    public @Nullable ItemIota selfOrNull() {
        if (MediafiedItemManager.contains(this.getItemIndex()))
            return this;
        return null;
    }

    public int getItemIndex() {
        return (int) payload;
    }

    public Item getItem() {
        return Objects.requireNonNull(MediafiedItemManager.getItem(this.getItemIndex()), "MediafiedItemManager returned null for Item that has existing ItemIota.");
    }

    public CompoundTag getTag() {
        return Objects.requireNonNull(MediafiedItemManager.getTag(this.getItemIndex()), "MediafiedItemManager returned null for Item that has existing ItemIota.");
    }

    public long getCount() {
        return Objects.requireNonNull(MediafiedItemManager.getCount(this.getItemIndex()), "MediafiedItemManager returned null for Item that has existing ItemIota.");
    }

    public void absorb(ItemIota other) {
        MediafiedItemManager.merge(this.getItemIndex(), other.getItemIndex());
    }

    public List<ItemStack> getStacksToDrop(int count) {
        return MediafiedItemManager.getStacksToDrop(this.getItemIndex(), count);
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
        tag.putLong(TAG_COUNT, rec.getCount());

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

            return Component.translatable("hexal.spelldata.item", ctag.getString(TAG_DISPLAY_NAME), ctag.getLong(TAG_COUNT)).withStyle(ChatFormatting.AQUA);
        }

        @Override
        public int color() {
            return 0xff_55ffff;
        }
    };
}
