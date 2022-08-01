package ram.talia.hexal.fabric

import ram.talia.hexal.api.HexalAPI
import net.fabricmc.api.ModInitializer

object FabricHexalInitializer : ModInitializer {
    override fun onInitialize() {
        HexalAPI.LOGGER.info("Hello Fabric World!"
    }
}