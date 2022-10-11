package ram.talia.hexal.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.network.IMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import ram.talia.hexal.api.everbook.Everbook;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.linkable.PlayerLinkstore;
import ram.talia.hexal.api.spell.casting.WispCastingManager;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface IXplatAbstractions {
//    Platform platform();
//
//    boolean isModPresent(String id);
//
//    boolean isPhysicalClient();
//
//    void initPlatformSpecific();

    void sendPacketToPlayer(ServerPlayer target, IMessage packet);

    void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet);

    // https://github.com/VazkiiMods/Botania/blob/13b7bcd9cbb6b1a418b0afe455662d29b46f1a7f/Xplat/src/main/java/vazkii/botania/xplat/IXplatAbstractions.java#L157
    Packet<?> toVanillaClientboundPacket(IMessage message);

//    double getReachDistance(Player player);

    // Things that used to be caps

    WispCastingManager getWispCastingManager(ServerPlayer caster);
    
    PlayerLinkstore getLinkstore(ServerPlayer player);
    
    void syncAddRenderLinkPlayer (ServerPlayer player, ILinkable<?> link);
    
    void syncRemoveRenderLinkPlayer (ServerPlayer player, ILinkable<?> link);
    
    //region Transmission
    ILinkable<?> getPlayerTransmittingTo (ServerPlayer player);
    void setPlayerTransmittingTo (ServerPlayer player, int to);
    void resetPlayerTransmittingTo (ServerPlayer player);
    //endregion
    
    SpellDatum<?> getEverbookIota(ServerPlayer player, HexPattern key);
    void setEverbookIota(ServerPlayer player, HexPattern key, SpellDatum<?> iota);
    
    void removeEverbookIota(ServerPlayer player, HexPattern key);
    
    void setFullEverbook(ServerPlayer player, Everbook everbook);
    
    List<SpellDatum<?>> getEverbookMacro (ServerPlayer player, HexPattern key);
    
    void toggleEverbookMacro (ServerPlayer player, HexPattern key);
    
    // Blocks

//    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> func,
//        Block... blocks);
//
//    boolean tryPlaceFluid(Level level, InteractionHand hand, BlockPos pos, ItemStack stack, Fluid fluid);


    // misc


//    boolean isCorrectTierForDrops(Tier tier, BlockState bs);
//
//    ResourceLocation getID(Block block);
//
//    ResourceLocation getID(Item item);
//
//    ResourceLocation getID(VillagerProfession profession);
//
//    Ingredient getUnsealedIngredient(ItemStack stack);
//
//    IXplatTags tags();
//
//    LootItemCondition.Builder isShearsCondition();
//
//    String getModName(String namespace);
//
//    boolean isBreakingAllowed(Level world, BlockPos pos, BlockState state, Player player);
//
//    boolean isPlacingAllowed(Level world, BlockPos pos, ItemStack blockStack, Player player);

    // interop

//    PehkuiInterop.ApiAbstraction getPehkuiApi();

    ///

    IXplatAbstractions INSTANCE = find();

    private static IXplatAbstractions find() {
        var providers = ServiceLoader.load(IXplatAbstractions.class).stream().toList();
        if (providers.size() != 1) {
            var names = providers.stream().map(p -> p.type().getName()).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException(
                "There should be exactly one IXplatAbstractions implementation on the classpath. Found: " + names);
        } else {
            var provider = providers.get(0);
            HexAPI.LOGGER.debug("Instantiating xplat impl: " + provider.type().getName());
            return provider.get();
        }
    }

}
