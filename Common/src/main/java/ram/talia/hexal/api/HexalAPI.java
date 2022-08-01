package ram.talia.hexal.api;

import com.google.common.base.Suppliers;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public interface HexalAPI
{
	String MOD_ID = "hexal";
	Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	Supplier<HexalAPI> INSTANCE = Suppliers.memoize(() -> {
		try {
			return (HexalAPI) Class.forName("com.talia.hexal.common.impl.HexalAPIImpl")
								 .getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			LogManager.getLogger().warn("Unable to find HexalAPIImpl, using a dummy");
			return new HexalAPI() {
			};
		}
	});
	
	static HexalAPI instance() {
		return INSTANCE.get();
	}
	
	static ResourceLocation modLoc(String s) {
		return new ResourceLocation(MOD_ID, s);
	}
}
