package ram.talia.hexal.fabric.interop.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HexalREIPlugin implements REIClientPlugin {
	
	@Override
	public void registerCategories (CategoryRegistry registry) {
		return;
	}
}