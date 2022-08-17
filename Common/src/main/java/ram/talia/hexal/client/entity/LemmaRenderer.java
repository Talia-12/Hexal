package ram.talia.hexal.client.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.common.entities.BaseLemma;

import static ram.talia.hexal.api.HexalAPI.modLoc;

public class LemmaRenderer extends EntityRenderer<BaseLemma> {
	private static final ResourceLocation LEMMA = modLoc("textures/entity/lemma.png");
	
	public LemmaRenderer (EntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public @NotNull ResourceLocation getTextureLocation (@NotNull BaseLemma baseLemma) {
		return LEMMA;
	}
}
