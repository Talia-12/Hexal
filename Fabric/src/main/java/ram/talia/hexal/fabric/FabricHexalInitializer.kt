package ram.talia.hexal.fabric

import ram.talia.hexal.api.HexalAPI
import net.fabricmc.api.ModInitializer
import ram.talia.hexal.common.casting.RegisterPatterns

object FabricHexalInitializer : ModInitializer {
    override fun onInitialize() {
        HexalAPI.LOGGER.info("Hello Fabric World!")

        RegisterPatterns.registerPatterns()
    }
}