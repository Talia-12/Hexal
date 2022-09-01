package ram.talia.hexal.forge;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ram.talia.hexal.client.RegisterClientStuff;

public class ForgeHexalClientInitializer {
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		event.enqueueWork(RegisterClientStuff::init);
	}
}
