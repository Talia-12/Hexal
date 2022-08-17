package ram.talia.hexal.client;

import ram.talia.hexal.client.entity.LemmaRenderer;
import ram.talia.hexal.common.entities.HexalEntities;
import ram.talia.hexal.xplat.IClientXplatAbstractions;

public class RegisterClientStuff {
	public static void init () {
		var x = IClientXplatAbstractions.INSTANCE;
		
		x.registerEntityRenderer(HexalEntities.PROJECTILE_LEMMA, LemmaRenderer::new);
		x.registerEntityRenderer(HexalEntities.TICKING_LEMMA, LemmaRenderer::new);
	}
}
