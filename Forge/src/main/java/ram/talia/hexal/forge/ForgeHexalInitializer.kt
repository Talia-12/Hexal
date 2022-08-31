package ram.talia.hexal.forge

import at.petrak.hexcasting.forge.datagen.HexForgeDataGenerators
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.casting.RegisterPatterns
import ram.talia.hexal.common.entities.HexalEntities
import ram.talia.hexal.common.recipe.HexalRecipeSerializers
import ram.talia.hexal.forge.datagen.HexalForgeDataGenerators
import thedarkcolour.kotlinforforge.KotlinModLoadingContext.Companion.get
import java.util.function.BiConsumer
import java.util.function.Consumer

@Mod(HexalAPI.MOD_ID)
class ForgeHexalInitializer {

	init {
		initConfig()
		initRegistry()
		initListeners()
	}

	private fun initConfig() {

	}

	private fun initRegistry() {
		bind(ForgeRegistries.RECIPE_SERIALIZERS, HexalRecipeSerializers::registerSerializers)

		bind(ForgeRegistries.ENTITIES, HexalEntities::registerEntities)


	}

	private fun initListeners() {
		val modBus = getModEventBus()
		val evBus = MinecraftForge.EVENT_BUS

		modBus.register(ForgeHexalClientInitializer::class.java)

		modBus.addListener { evt: FMLCommonSetupEvent ->
			evt.enqueueWork {
				RegisterPatterns.registerPatterns()
			}
		}

		// We have to do these at some point when the registries are still open
		modBus.addGenericListener(Item::class.java) { evt: RegistryEvent<Item> -> HexalRecipeSerializers.registerTypes() }

		modBus.register(HexalForgeDataGenerators::class.java)
	}

	// https://github.com/VazkiiMods/Botania/blob/1.18.x/Forge/src/main/java/vazkii/botania/forge/ForgeCommonInitializer.java
	private fun <T : IForgeRegistryEntry<T>> bind(registry: IForgeRegistry<T>, source: Consumer<BiConsumer<T, ResourceLocation>>) {
		getModEventBus().addGenericListener(
			registry.registrySuperType
		) { event: RegistryEvent.Register<T> ->
			val forgeRegistry = event.registry
			source.accept(BiConsumer { t: T, rl: ResourceLocation? ->
				t.registryName = rl
				forgeRegistry.register(t)
			})
		}
	}

	private fun getModEventBus(): IEventBus {
		return get().getKEventBus()
	}
}