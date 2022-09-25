package ram.talia.hexal.forge;

import at.petrak.hexcasting.common.blocks.behavior.HexComposting;
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables;
import at.petrak.hexcasting.common.misc.AkashicTreeGrower;
import at.petrak.hexcasting.interop.HexInterop;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.common.casting.RegisterPatterns;
import ram.talia.hexal.common.lib.*;
import ram.talia.hexal.common.lib.feature.HexalConfiguredFeatures;
import ram.talia.hexal.common.lib.feature.HexalFeatures;
import ram.talia.hexal.common.lib.feature.HexalPlacedFeatures;
import ram.talia.hexal.common.recipe.HexalRecipeSerializers;
import ram.talia.hexal.forge.cap.CapSyncers;
import ram.talia.hexal.forge.datagen.HexalForgeDataGenerators;
import ram.talia.hexal.forge.eventhandlers.BiomeGenerationEventHandler;
import ram.talia.hexal.forge.eventhandlers.PlayerLinkstoreEventHandler;
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
	
	}
	
	private static void initRegistry () {
		bind(ForgeRegistries.FEATURES, HexalFeatures::registerFeatures);
		bind(BuiltinRegistries.CONFIGURED_FEATURE, HexalConfiguredFeatures::registerConfiguredFeatures);
		bind(BuiltinRegistries.PLACED_FEATURE, HexalPlacedFeatures::registerPlacedFeatures);
		
		bind(ForgeRegistries.SOUND_EVENTS, HexalSounds::registerSounds);
		bind(ForgeRegistries.BLOCKS, HexalBlocks::registerBlocks);
		bind(ForgeRegistries.ITEMS, HexalBlocks::registerBlockItems);
		bind(ForgeRegistries.BLOCK_ENTITIES, HexalBlockEntities::registerBlockEntities);
		bind(ForgeRegistries.ENTITIES, HexalEntities::registerEntities);
		
		bind(ForgeRegistries.RECIPE_SERIALIZERS, HexalRecipeSerializers::registerSerializers);
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
		modBus.addGenericListener(Item.class, (RegistryEvent<Item> evt) -> HexalRecipeSerializers.registerTypes());
		
		modBus.register(HexalForgeDataGenerators.class);
		
		evBus.register(WispCastingMangerEventHandler.class);
		evBus.register(CapSyncers.class);
		evBus.register(PlayerLinkstoreEventHandler.class);
		evBus.register(BiomeGenerationEventHandler.class);
	}
	
	// https://github.com/VazkiiMods/Botania/blob/1.18.x/Forge/src/main/java/vazkii/botania/forge/ForgeCommonInitializer.java
	private static <T extends IForgeRegistryEntry<T>> void bind (IForgeRegistry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
		getModEventBus().addGenericListener(registry.getRegistrySuperType(), (RegistryEvent.Register<T> event) -> {
			IForgeRegistry<T> forgeRegistry = event.getRegistry();
			source.accept((t, rl) -> {
				t.setRegistryName(rl);
				forgeRegistry.register(t);
			});
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