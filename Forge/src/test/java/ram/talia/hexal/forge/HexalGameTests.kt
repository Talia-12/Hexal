package ram.talia.hexal.forge

import net.minecraft.SharedConstants
import net.minecraftforge.event.RegisterGameTestsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import ram.talia.hexal.api.HexalAPI

@EventBusSubscriber(modid = HexalAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object HexalGameTests {
	@SubscribeEvent
	fun registerTests(event: RegisterGameTestsEvent) {
		SharedConstants.IS_RUNNING_IN_IDE = true
		HexalAPI.LOGGER.debug("registering tests")
		event.register(WispTests::class.java)
	}
}