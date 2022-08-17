package ram.talia.hexal.xplat;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.server.level.ServerPlayer;
import ram.talia.hexal.api.spell.casting.LemmaCastingManager;

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

//    void sendPacketToPlayer(ServerPlayer target, IMessage packet);

//    void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet);

    // https://github.com/VazkiiMods/Botania/blob/13b7bcd9cbb6b1a418b0afe455662d29b46f1a7f/Xplat/src/main/java/vazkii/botania/xplat/IXplatAbstractions.java#L157
//    Packet<?> toVanillaClientboundPacket(IMessage message);

//    double getReachDistance(Player player);

    // Things that used to be caps

    LemmaCastingManager getLemmaCastingManager (ServerPlayer caster);
    
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
