package ram.talia.hexal.fabric

import net.fabricmc.api.ClientModInitializer
import ram.talia.hexal.client.RegisterClientStuff

object FabricHexalClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        RegisterClientStuff.init()
    }
}