package ram.talia.hexal.forge;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ram.talia.hexal.client.RegisterClientStuff;
import ram.talia.hexal.common.lib.HexalBlockEntities;
import ram.talia.hexal.fabric.client.blocks.BlockEntityRelayRenderer;

public class ForgeHexalClientInitializer {
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		event.enqueueWork(RegisterClientStuff::init);
	}
	
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
		RegisterClientStuff.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
		evt.registerBlockEntityRenderer(HexalBlockEntities.RELAY, BlockEntityRelayRenderer::new);
	}
}
