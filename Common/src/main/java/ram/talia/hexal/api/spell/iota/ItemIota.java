package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.spell.iota.NullIota;
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
import ram.talia.hexal.api.mediafieditems.ItemRecord;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.common.lib.HexalIotaTypes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Similar to GateIotas, stores a reference to an item stored in the
 * media. When the item is used up, all references to it become null.
 */
public class ItemIota extends Iota {
    static final String TAG_DISPLAY_NAME = "name";
    static final String TAG_COUNT = "count";

    public ItemIota(MediafiedItemManager.Index payload) {
        super(HexalIotaTypes.ITEM, payload);
    }

    public static @Nullable ItemIota makeIfStorageLoaded(ItemStack stack, UUID storageUUID) {
        var index = MediafiedItemManager.assignItem(stack, storageUUID);

        if (index != null)
            return new ItemIota(index);
        else
            return null;
    }

    public static @Nullable ItemIota makeIfStorageLoaded(ItemRecord record, UUID storageUUID) {
        var index = MediafiedItemManager.assignItem(record, storageUUID);

        if (index != null)
            return new ItemIota(index);
        else
            return null;
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

    public boolean isEmpty() {
        return !MediafiedItemManager.contains(this.getItemIndex());
    }

    public MediafiedItemManager.Index getItemIndex() {
        return (MediafiedItemManager.Index) payload;
    }

    public @Nullable ItemRecord getRecord() {
        var record = MediafiedItemManager.getRecord(this.getItemIndex());
        if (record == null)
            return null;
        return record.get();
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

    public @Nullable ItemIota splitOff(int amount, @Nullable UUID storage) {
        var newIndex = MediafiedItemManager.splitOff(this.getItemIndex(), amount, storage);
        if (newIndex == null)
            return null;

        return new ItemIota(newIndex);
    }

    public List<ItemStack> getStacksToDrop(int count) {
        return MediafiedItemManager.getStacksToDrop(this.getItemIndex(), count);
    }

    public long removeItems(int count) {
        return removeItems((long) count);
    }

    public long removeItems(long count) {
        return MediafiedItemManager.removeItems(this.getItemIndex(), count);
    }

    /**
     * Takes a template ItemStack and sets the item and tag of the referenced ItemRecord to that item and tag, while leaving the count the same.
     */
    public void templateOff(@NotNull ItemStack template) {
        MediafiedItemManager.templateOff(this.getItemIndex(), template);
    }

    public ItemIota copy() {
        return new ItemIota(this.getItemIndex());
    }

    public @Nullable ItemIota setStorage(@NotNull UUID uuid) {
        var storageFull = MediafiedItemManager.isStorageFull(uuid);
        if (storageFull == null || storageFull) // isStorageFull can return null
            return null;

        var record = getRecord();
        MediafiedItemManager.removeRecord(this.getItemIndex());
        if (record == null)
            return null;

        var newIndex = MediafiedItemManager.assignItem(record, uuid);
        return new ItemIota(newIndex);
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return (typesMatch(this, that) &&
                that instanceof ItemIota ithat &&
                this.getItemIndex() == ithat.getItemIndex())
                || (this.isEmpty() && that instanceof NullIota);
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
        this.getItemIndex().writeToNbt(tag);

        var record = MediafiedItemManager.getRecord(this.getItemIndex());

        ItemRecord rec;

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

            var index = MediafiedItemManager.Index.readFromNbt(ctag);

            if (!MediafiedItemManager.contains(index))
                return null;

            return new ItemIota(index);
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof CompoundTag ctag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }

            if (!ctag.contains(TAG_DISPLAY_NAME) || !ctag.contains(TAG_COUNT))
                return Component.translatable("hexcasting.tooltip.null_iota").withStyle(ChatFormatting.GRAY);

            return Component.translatable("hexal.spelldata.item", ctag.getString(TAG_DISPLAY_NAME), ctag.getLong(TAG_COUNT)).withStyle(ChatFormatting.YELLOW);
        }

        @Override
        public int color() {
            return 0xff_ffff55;
        }
    };
}
