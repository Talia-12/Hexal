package ram.talia.hexal.fabric

import ram.talia.hexal.api.HexalAPI
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import ram.talia.hexal.common.casting.RegisterPatterns
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.entities.HexalEntities
import java.util.function.BiConsumer

object FabricHexalInitializer : ModInitializer {
    override fun onInitialize() {
        HexalAPI.LOGGER.info("Hello Fabric World!")

        RegisterPatterns.registerPatterns()
        HexalEntities.registerEntities(bind(Registry.ENTITY_TYPE))
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}