package ram.talia.hexal.client.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.common.entities.BaseWisp;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class WispRenderer extends EntityRenderer<BaseWisp> {
	private static final ResourceLocation WISP = modLoc("textures/entity/wisp.png");
	
	public WispRenderer (EntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public @NotNull ResourceLocation getTextureLocation (@NotNull BaseWisp baseWisp) {
		return WISP;
	}
}
