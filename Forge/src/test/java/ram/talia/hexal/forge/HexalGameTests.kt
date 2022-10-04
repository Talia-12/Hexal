package ram.talia.hexal.forge

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.event.RegisterGameTestsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import ram.talia.hexal.api.HexalAPI
import kotlin.io.path.Path
import kotlin.io.path.absolute

@EventBusSubscriber(modid = HexalAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public object HexalGameTests {
	@SubscribeEvent
	public fun registerTests(event: RegisterGameTestsEvent) {
		HexalAPI.LOGGER.debug("registering tests")
		event.register(ExampleTests::class.java)
	}
}