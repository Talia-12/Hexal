package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.lib.HexalIotaTypes;

public class ItemTypeIota extends Iota {
    public ItemTypeIota(@NotNull Item item) {
        super(HexalIotaTypes.ITEM_TYPE, Either.left(item));
    }
    public ItemTypeIota(@NotNull Block block) {
        super(HexalIotaTypes.ITEM_TYPE, Either.right(block));
    }

    @SuppressWarnings("unchecked")
    public Either<Item, Block> getEither() {
        return (Either<Item, Block>) this.payload;
    }

    @Nullable
    public Block getBlock() {
        return this.getEither().map(item -> {
            if (item instanceof BlockItem blockItem)
                return blockItem.getBlock();
            else return null;
        }, block -> block);
    }

    /**
     * If the block has no item form this returns Items.AIR
     */
    @Nullable
    public Item getItem() {
        return this.getEither().map(item -> item, Block::asItem);
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) &&
                that instanceof ItemTypeIota dent &&
                this.getEither().map(
                        itemThis -> dent.getEither().map(itemThis::equals,
                                                         blockThat -> {
                                                             var itemThat = blockThat.asItem();
                                                             if (itemThat.equals(Items.AIR) && !blockThat.equals(Blocks.AIR))
                                                                 return false;
                                                             return itemThis.equals(itemThat);
                                                         }),
                        blockThis -> dent.getEither().map(itemThat -> {
                                                              var itemThis = blockThis.asItem();
                                                              if (itemThis.equals(Items.AIR) && !blockThis.equals(Blocks.AIR))
                                                                  return false;
                                                              return itemThis.equals(itemThat);
                                                          }, blockThis::equals));
    }

    @Override
    public boolean isTruthy() {
        return this.getEither().map(item -> !item.equals(Items.AIR), block -> !block.equals(Blocks.AIR));
    }

    @Override
    public @NotNull Tag serialize() {
        var tag = new CompoundTag();
        return this.getEither().map(item -> {
            tag.putString(TAG_ITEM, Registry.ITEM.getKey(item).toString());
            return tag;
        }, block -> {
            tag.putString(TAG_BLOCK, Registry.BLOCK.getKey(block).toString());
            return tag;
        });
    }

    public static IotaType<ItemTypeIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public ItemTypeIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var ctag = HexUtils.downcast(tag, CompoundTag.TYPE);

            if (ctag.contains(TAG_ITEM)) {
                var typeLocation = ResourceLocation.read(ctag.getString(TAG_ITEM));
                var type = typeLocation.map(Registry.ITEM::get).get().left();

                return type.map(ItemTypeIota::new).orElse(null);
            } else if (ctag.contains(TAG_BLOCK)) {
                var typeLocation = ResourceLocation.read(ctag.getString(TAG_BLOCK));
                var type = typeLocation.map(Registry.BLOCK::get).get().left();

                return type.map(ItemTypeIota::new).orElse(null);
            }

            return null;
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof CompoundTag ctag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }
            if (ctag.contains(TAG_ITEM)) {
                var typeLocation = ResourceLocation.read(ctag.getString(TAG_ITEM));
                var type = typeLocation.map(Registry.ITEM::get).get().left();

                return type.map(t -> t.getDescription().copy().withStyle(ChatFormatting.GOLD))
                        .orElse(Component.translatable("hexcasting.spelldata.unknown"));
            } else if (ctag.contains(TAG_BLOCK)) {
                var typeLocation = ResourceLocation.read(ctag.getString(TAG_BLOCK));
                var type = typeLocation.map(Registry.BLOCK::get).get().left();

                return type.map(b -> b.getName().withStyle(ChatFormatting.GOLD))
                        .orElse(Component.translatable("hexcasting.spelldata.unknown"));
            }

            return Component.translatable("hexcasting.spelldata.unknown");
        }

        @Override
        public int color() {
            return 0xff_feaa01;
        }
    };

    private static final String TAG_ITEM = "item";
    private static final String TAG_BLOCK = "block";
}
