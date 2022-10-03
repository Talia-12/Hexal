package ram.talia.hexal.forge

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.event.RegisterGameTestsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import ram.talia.hexal.api.HexalAPI

@EventBusSubscriber(modid = HexalAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object HexalHameTests {
	@SubscribeEvent
	fun registerTests(event: RegisterGameTestsEvent) {
		event.register(HexalGameTests::class.java)
	}

	@GameTest
	fun exampleTest(helper: GameTestHelper) {
		HexalAPI.LOGGER.debug("asdfasdf")
	}
}