package ram.talia.hexal.forge;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import ram.talia.hexal.client.RegisterClientStuff;
import ram.talia.hexal.common.lib.HexalBlockEntities;
import ram.talia.hexal.common.lib.HexalItems;
import ram.talia.hexal.forge.client.blocks.BlockEntityRelayRenderer;
import ram.talia.hexal.forge.client.items.ItemRelayRenderer;
import ram.talia.hexal.forge.client.items.IRenderPropertiesSetter;

public class ForgeHexalClientInitializer {
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		event.enqueueWork(RegisterClientStuff::init);

		if (FMLEnvironment.dist == Dist.CLIENT && !FMLLoader.getLaunchHandler().isData())
			cursedItemPropertiesNonsense();
	}

	private static void cursedItemPropertiesNonsense() {
		// this is *so* dumb
		//noinspection DataFlowIssue
		((IRenderPropertiesSetter) (Object) HexalItems.RELAY).setRenderProperties(new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ItemRelayRenderer();

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}
	
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
		RegisterClientStuff.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
		evt.registerBlockEntityRenderer(HexalBlockEntities.RELAY, BlockEntityRelayRenderer::new);
	}
}
