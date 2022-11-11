package ram.talia.hexal.api.spell.iota;

import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.lib.HexalIotaTypes;

public class BlockTypeIota extends Iota {
    public BlockTypeIota(@NotNull Block block) {
        super(HexalIotaTypes.BLOCK_TYPE, block);
    }

    public Block getBlock() {
        return (Block) this.payload;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) &&
                that instanceof BlockTypeIota dent &&
                this.getBlock().equals(dent.getBlock());
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public @NotNull Tag serialize() {
        return StringTag.valueOf(Registry.BLOCK.getKey(this.getBlock()).toString());
    }

    public static IotaType<BlockTypeIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public BlockTypeIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var stag = HexUtils.downcast(tag, StringTag.TYPE);

            var typeLocation = ResourceLocation.read(stag.getAsString());
            var type = typeLocation.map(Registry.BLOCK::get).get().left();

            return type.map(BlockTypeIota::new).orElse(null);
        }

        @Override
        public Component display(Tag tag) {
            if (!(tag instanceof StringTag stag)) {
                return Component.translatable("hexcasting.spelldata.unknown");
            }
            var typeLocation = ResourceLocation.read(stag.getAsString());
            var type = typeLocation.map(Registry.BLOCK::get).get().left();

            return type.map(b -> b.getName().withStyle(ChatFormatting.DARK_GREEN))
                    .orElse(Component.translatable("hexcasting.spelldata.unknown"));
        }

        @Override
        public int color() {
            return 0xff_117711;
        }
    };
}
