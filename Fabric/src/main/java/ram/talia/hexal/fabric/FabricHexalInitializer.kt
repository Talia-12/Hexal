package ram.talia.hexal.fabric

import ram.talia.hexal.api.HexalAPI
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import ram.talia.hexal.common.casting.RegisterPatterns
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.client.LinkablePacketHolder
import ram.talia.hexal.common.lib.*
import ram.talia.hexal.common.lib.feature.HexalConfiguredFeatures
import ram.talia.hexal.common.lib.feature.HexalFeatures
import ram.talia.hexal.common.lib.feature.HexalPlacedFeatures
import ram.talia.hexal.common.recipe.HexalRecipeSerializers
import ram.talia.hexal.common.recipe.HexalRecipeTypes
import ram.talia.hexal.fabric.network.FabricPacketHandler
import java.util.function.BiConsumer

object FabricHexalInitializer : ModInitializer {
    override fun onInitialize() {
        HexalAPI.LOGGER.info("Hello Fabric World!")

        FabricPacketHandler.initServerBound()

        initListeners()

        initRegistries()

        RegisterPatterns.registerPatterns()
    }

    private fun initListeners() {
        // reattempt link render packets that failed to apply properly once every 20 ticks.
        ClientTickEvents.START_CLIENT_TICK.register { LinkablePacketHolder.maybeRetry() }
    }

    private fun initRegistries() {
        HexalFeatures.registerFeatures(bind(Registry.FEATURE))
        HexalConfiguredFeatures.registerConfiguredFeatures(bind(BuiltinRegistries.CONFIGURED_FEATURE))
        HexalPlacedFeatures.registerPlacedFeatures(bind(BuiltinRegistries.PLACED_FEATURE))
        HexalPlacedFeatures.placeGeodesInBiome { feature, decoration ->
            BuiltinRegistries.PLACED_FEATURE.getResourceKey(feature).ifPresent {
                BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), decoration, it)
            }
        }

        HexalSounds.registerSounds(bind(Registry.SOUND_EVENT))
        HexalBlocks.registerBlocks(bind(Registry.BLOCK))
        HexalBlocks.registerBlockItems(bind(Registry.ITEM))
        HexalBlockEntities.registerBlockEntities(bind(Registry.BLOCK_ENTITY_TYPE))
        HexalEntities.registerEntities(bind(Registry.ENTITY_TYPE))

        HexalRecipeSerializers.registerSerializers(bind(Registry.RECIPE_SERIALIZER))
        HexalRecipeTypes.registerTypes(bind(Registry.RECIPE_TYPE))

        HexalIotaTypes.registerTypes()
    }


    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}