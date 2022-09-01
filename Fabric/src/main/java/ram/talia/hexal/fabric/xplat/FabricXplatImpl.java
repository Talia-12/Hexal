package ram.talia.hexal.fabric.xplat;

import net.minecraft.server.level.ServerPlayer;
import ram.talia.hexal.api.spell.casting.WispCastingManager;
import ram.talia.hexal.fabric.cc.HexalCardinalComponents;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.Optional;

public class FabricXplatImpl implements IXplatAbstractions {
//    @Override
//    public Platform platform() {
//        return Platform.FABRIC;
//    }

//    @Override
//    public boolean isPhysicalClient() {
//        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
//    }
//
//    @Override
//    public boolean isModPresent(String id) {
//        return FabricLoader.getInstance().isModLoaded(id);
//    }
//
//    @Override
//    public void initPlatformSpecific() {
//        if (this.isModPresent(HexInterop.Fabric.GRAVITY_CHANGER_API_ID)) {
//            GravityApiInterop.init();
//        }
//        if (this.isModPresent(HexInterop.Fabric.TRINKETS_API_ID)) {
//            TrinketsApiInterop.init();
//        }
//    }

//    @Override
//    public double getReachDistance(Player player) {
//        return ReachEntityAttributes.getReachDistance(player, 5.0);
//    }

//    @Override
//    public void sendPacketToPlayer(ServerPlayer target, IMessage packet) {
//        ServerPlayNetworking.send(target, packet.getFabricId(), packet.toBuf());
//    }
//
//    @Override
//    public void sendPacketNear(Vec3 pos, double radius, ServerLevel dimension, IMessage packet) {
//        var pkt = ServerPlayNetworking.createS2CPacket(packet.getFabricId(), packet.toBuf());
//        var nears = PlayerLookup.around(dimension, pos, radius);
//        for (var p : nears) {
//            p.connection.send(pkt);
//        }
//    }
//
//    @Override
//    public Packet<?> toVanillaClientboundPacket(IMessage message) {
//        return ServerPlayNetworking.createS2CPacket(message.getFabricId(), message.toBuf());
//    }
    
    @Override
    public Optional<WispCastingManager> getWispCastingManager (ServerPlayer caster) {
        var cc = HexalCardinalComponents.WISP_CASTING_MANAGER.get(caster);
        return Optional.ofNullable(cc.getManager());
    }

//    @Override
//    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> func,
//        Block... blocks) {
//        return FabricBlockEntityTypeBuilder.create(func::apply, blocks).build();
//    }
//
//    @Override
//    @SuppressWarnings("UnstableApiUsage")
//    public boolean tryPlaceFluid(Level level, InteractionHand hand, BlockPos pos, ItemStack stack, Fluid fluid) {
//        Storage<FluidVariant> target = FluidStorage.SIDED.find(level, pos, Direction.UP);
//        Storage<FluidVariant> emptyFrom = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack));
//        return StorageUtil.move(emptyFrom, target, (f) -> true, FluidConstants.BUCKET, null) > 0;
//    }
//
//    @Override
//    public ResourceLocation getID(Block block) {
//        return Registry.BLOCK.getKey(block);
//    }
//
//    @Override
//    public ResourceLocation getID(Item item) {
//        return Registry.ITEM.getKey(item);
//    }
//
//    @Override
//    public ResourceLocation getID(VillagerProfession profession) {
//        return Registry.VILLAGER_PROFESSION.getKey(profession);
//    }
//
//    @Override
//    public Ingredient getUnsealedIngredient(ItemStack stack) {
//        return FabricUnsealedIngredient.of(stack);
//    }
//
//    private static CreativeModeTab TAB = null;
//
//    @Override
//    public CreativeModeTab getTab() {
//        if (TAB == null) {
//            TAB = FabricItemGroupBuilder.create(modLoc("creative_tab"))
//                .icon(HexItems::tabIcon)
//                .build();
//        }
//
//        return TAB;
//    }
//
//    // do a stupid hack from botania
//    private static List<ItemStack> stacks(Item... items) {
//        return Stream.of(items).map(ItemStack::new).toList();
//    }
//
//    private static final List<List<ItemStack>> HARVEST_TOOLS_BY_LEVEL = List.of(
//        stacks(Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL),
//        stacks(Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE, Items.STONE_SHOVEL),
//        stacks(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SHOVEL),
//        stacks(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_SHOVEL),
//        stacks(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE, Items.NETHERITE_SHOVEL)
//    );
//
//    @Override
//    public boolean isCorrectTierForDrops(Tier tier, BlockState bs) {
//        if (!bs.requiresCorrectToolForDrops()) {
//            return true;
//        }
//
//        int level = HexConfig.server()
//            .opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort();
//        for (var tool : HARVEST_TOOLS_BY_LEVEL.get(level)) {
//            if (tool.isCorrectToolForDrops(bs)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public Item.Properties addEquipSlotFabric(EquipmentSlot slot) {
//        return new FabricItemSettings().equipmentSlot(s -> slot);
//    }
//
//    private static final IXplatTags TAGS = new IXplatTags() {
//        @Override
//        public TagKey<Item> amethystDust() {
//            return HexItemTags.create(new ResourceLocation("c", "amethyst_dusts"));
//        }
//
//        @Override
//        public TagKey<Item> gems() {
//            return HexItemTags.create(new ResourceLocation("c", "gems"));
//        }
//    };
//
//    @Override
//    public IXplatTags tags() {
//        return TAGS;
//    }
//
//    @Override
//    public LootItemCondition.Builder isShearsCondition() {
//        return AlternativeLootItemCondition.alternative(
//            MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)),
//            MatchTool.toolMatches(ItemPredicate.Builder.item().of(
//                HexItemTags.create(new ResourceLocation("c", "shears"))))
//        );
//    }
//
//    @Override
//    public String getModName(String namespace) {
//        if (namespace.equals("c")) {
//            return "Common";
//        }
//        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(namespace);
//        if (container.isPresent()) {
//            return container.get().getMetadata().getName();
//        }
//        return namespace;
//    }
//
//    @Override
//    public boolean isBreakingAllowed(Level world, BlockPos pos, BlockState state, Player player) {
//        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, player, pos, state, world.getBlockEntity(pos));
//    }
//
//    @Override
//    public boolean isPlacingAllowed(Level world, BlockPos pos, ItemStack blockStack, Player player) {
//        ItemStack cached = player.getMainHandItem();
//        player.setItemInHand(InteractionHand.MAIN_HAND, blockStack.copy());
//        var success = UseItemCallback.EVENT.invoker().interact(player, world, InteractionHand.MAIN_HAND);
//        player.setItemInHand(InteractionHand.MAIN_HAND, cached);
//        return success.getResult() == InteractionResult.PASS; // No other mod tried to consume this
//    }
}
