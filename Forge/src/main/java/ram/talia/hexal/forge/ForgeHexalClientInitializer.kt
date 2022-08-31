package ram.talia.hexal.forge

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import ram.talia.hexal.client.RegisterClientStuff

class ForgeHexalClientInitializer {
	@SubscribeEvent
	fun clientInit(evt: FMLClientSetupEvent) {
		evt.enqueueWork { RegisterClientStuff.init() }
	}

//	@SubscribeEvent
//	fun registerRenderers(evt: RegisterRenderers) {
//		RegisterClientStuff.somethingsomething register entityrenderers here probably
//	}
}