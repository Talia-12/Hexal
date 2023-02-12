package ram.talia.hexal.forge;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.config.HexalConfig;
import ram.talia.hexal.common.casting.RegisterPatterns;
import ram.talia.hexal.common.lib.*;
import ram.talia.hexal.common.lib.feature.HexalConfiguredFeatures;
import ram.talia.hexal.common.lib.feature.HexalFeatures;
import ram.talia.hexal.common.lib.feature.HexalPlacedFeatures;
import ram.talia.hexal.common.recipe.HexalRecipeSerializers;
import ram.talia.hexal.common.recipe.HexalRecipeTypes;
import ram.talia.hexal.forge.datagen.HexalForgeDataGenerators;
import ram.talia.hexal.forge.eventhandlers.EverbookEventHandler;
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler;
import ram.talia.hexal.forge.eventhandlers.RenderLinkEventHandler;
import ram.talia.hexal.forge.eventhandlers.WispCastingMangerEventHandler;
import ram.talia.hexal.forge.network.ForgePacketHandler;
import thedarkcolour.kotlinforforge.KotlinModLoadingContext;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(HexalAPI.MOD_ID)
public class ForgeHexalInitializer {
	
	public ForgeHexalInitializer () {
		HexalAPI.LOGGER.info("Hello Forge World!");
		initConfig();
		initRegistry();
		initListeners();
	}
	
	private static void initConfig () {
		Pair<ForgeHexalConfig, ForgeConfigSpec> config = (new ForgeConfigSpec.Builder()).configure(ForgeHexalConfig::new);
		Pair<ForgeHexalConfig.Client, ForgeConfigSpec> clientConfig = (new ForgeConfigSpec.Builder()).configure(ForgeHexalConfig.Client::new);
		Pair<ForgeHexalConfig.Server, ForgeConfigSpec> serverConfig = (new ForgeConfigSpec.Builder()).configure(ForgeHexalConfig.Server::new);
		HexalConfig.setCommon(config.getLeft());
		HexalConfig.setClient(clientConfig.getLeft());
		HexalConfig.setServer(serverConfig.getLeft());
		ModLoadingContext mlc = ModLoadingContext.get();
		mlc.registerConfig(ModConfig.Type.COMMON, config.getRight());
		mlc.registerConfig(ModConfig.Type.CLIENT, clientConfig.getRight());
		mlc.registerConfig(ModConfig.Type.SERVER, serverConfig.getRight());
	}
	
	private static void initRegistry () {
		bind(Registry.FEATURE_REGISTRY, HexalFeatures::registerFeatures);
		bind(BuiltinRegistries.CONFIGURED_FEATURE, HexalConfiguredFeatures::registerConfiguredFeatures);
		bind(BuiltinRegistries.PLACED_FEATURE, HexalPlacedFeatures::registerPlacedFeatures);
		
		bind(Registry.SOUND_EVENT_REGISTRY, HexalSounds::registerSounds);
		bind(Registry.BLOCK_REGISTRY, HexalBlocks::registerBlocks);
		bind(Registry.ITEM_REGISTRY, HexalBlocks::registerBlockItems);
		bind(Registry.BLOCK_ENTITY_TYPE_REGISTRY, HexalBlockEntities::registerBlockEntities);
		bind(Registry.ENTITY_TYPE_REGISTRY, HexalEntities::registerEntities);
		
		bind(Registry.RECIPE_SERIALIZER_REGISTRY, HexalRecipeSerializers::registerSerializers);
		bind(Registry.RECIPE_TYPE_REGISTRY, HexalRecipeTypes::registerTypes);

		HexalIotaTypes.registerTypes();
	}
	
	private static void initListeners () {
		IEventBus modBus = getModEventBus();
		IEventBus evBus = MinecraftForge.EVENT_BUS;
		
		modBus.register(ForgeHexalClientInitializer.class);
		
		modBus.addListener((FMLCommonSetupEvent evt) ->
			 evt.enqueueWork(() -> {
				 //noinspection Convert2MethodRef
				 ForgePacketHandler.init();
			 }));
		
		modBus.addListener((FMLCommonSetupEvent evt) -> evt.enqueueWork(RegisterPatterns::registerPatterns));
		
		// We have to do these at some point when the registries are still open
//		modBus.addGenericListener(Item.class, (GenericEvent<Item> evt) -> HexalRecipeSerializers.registerTypes());
//		modBus.addListener((RegisterEvent evt) -> {
//			if (evt.getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
//				HexalRecipeSerializers.registerTypes();
//			}
//		});
		
		modBus.register(HexalForgeDataGenerators.class);

		evBus.register(EverbookEventHandler.class);
		evBus.register(PlayerLinkstoreEventHandler.class);
		evBus.register(RenderLinkEventHandler.class); // client only, might move into ForgeHexalClientInitializer if possible?
		evBus.register(WispCastingMangerEventHandler.class);
	}
	
	// https://github.com/VazkiiMods/Botania/blob/1.18.x/Forge/src/main/java/vazkii/botania/forge/ForgeCommonInitializer.java
	private static <T> void bind (ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
		getModEventBus().addListener((RegisterEvent event) -> {
			if (registry.equals(event.getRegistryKey())) {
				source.accept((t, rl) -> event.register(registry, rl, () -> t));
			}
		});
	}
	
	// This version of bind is used for BuiltinRegistries.
	private static <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
		source.accept((t, id) -> Registry.register(registry, id, t));
	}
	
	private static IEventBus getModEventBus () {
		return KotlinModLoadingContext.Companion.get().getKEventBus();
	}
}