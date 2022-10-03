package ram.talia.hexal.forge;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.api.HexalAPI;

public class HexalGameTests {
	@SubscribeEvent
	public void registerTests(RegisterGameTestsEvent event) {
		event.register(HexalGameTests.class);
	}
	
	@GameTest
	public static void exampleTest(GameTestHelper helper) {
		HexalAPI.LOGGER.debug("asdfasdf");
	}
}
