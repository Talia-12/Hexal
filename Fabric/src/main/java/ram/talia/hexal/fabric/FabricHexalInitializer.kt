package ram.talia.hexal.fabric

import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.HexalAPI.modLoc
import ram.talia.hexal.api.gates.GateSavedData
import ram.talia.hexal.common.casting.Patterns
import ram.talia.hexal.common.lib.*
import ram.talia.hexal.common.lib.feature.HexalConfiguredFeatures
import ram.talia.hexal.common.lib.feature.HexalFeatures
import ram.talia.hexal.common.lib.feature.HexalPlacedFeatures
import ram.talia.hexal.common.recipe.HexalRecipeSerializers
import ram.talia.hexal.common.recipe.HexalRecipeTypes
import ram.talia.hexal.fabric.interop.phantom.OpPhaseBlock
import ram.talia.hexal.fabric.network.FabricPacketHandler
import java.util.function.BiConsumer

object FabricHexalInitializer : ModInitializer {
    const val FILE_GATE_MANAGER = "hexal_gate_manager"

    override fun onInitialize() {
        HexalAPI.LOGGER.info("Hello Fabric World!")

        FabricHexalConfig.setup()
        FabricPacketHandler.initServerBound()

        initListeners()

        initRegistries()

        Patterns.registerPatterns()
    }

    private fun initListeners() {
        ServerLifecycleEvents.SERVER_STARTED.register {
            val savedData = it.overworld().dataStorage.computeIfAbsent(::GateSavedData, ::GateSavedData, FILE_GATE_MANAGER)
            savedData.setDirty()
        }
    }

    private fun initRegistries() {
        fabricOnlyRegistration()

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

    private fun fabricOnlyRegistration() {
        Patterns.make(HexPattern.fromAngles("daqqqa", HexDir.WEST), modLoc("interop/fabric_only/phase_block"), OpPhaseBlock, false)
    }


    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}