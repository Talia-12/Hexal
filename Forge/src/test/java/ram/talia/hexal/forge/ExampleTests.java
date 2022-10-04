package ram.talia.hexal.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import ram.talia.hexal.api.HexalAPI;

public class ExampleTests {
	@GameTest(templateNamespace = HexalAPI.MOD_ID, setupTicks = 40L)
	public static void basicTickingWispTest (GameTestHelper helper) {
		HexalAPI.LOGGER.debug("running basic ticking wisp test");
		
		for (int y=0; y <= 1; y++) {
			for (int x = 0; x <= 2; x++) {
				for (int z = 0; z <= 2; z++) {
					HexalAPI.LOGGER.debug("pos: (%d, %d, %d), block: %s".formatted(x,y,z, helper.getBlockState(new BlockPos(x,y,z))));
				}
			}
		}
	
		helper.succeed();
	}
}
