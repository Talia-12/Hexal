package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface;
import ram.talia.hexal.common.entities.BaseLemma;

import java.util.List;

@Mixin(CastingHarness.class)
public abstract class MixinCastingHarness {
	
	@Redirect(method = "updateWithPattern",
						at = @At(
									value="INVOKE",
									target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
					))
	private boolean updateWithPatternLemma (List<OperatorSideEffect> sideEffects, Object o) {
		
		if (o instanceof OperatorSideEffect.Particles particles) {
			
			CastingContext ctx = ((CastingHarness)(Object)this).getCtx();
			MixinCastingContextInterface ctxi = (MixinCastingContextInterface)(Object) ctx;
			
			if (!ctxi.hasLemma())
				return sideEffects.add(particles);
		}
		
		return false;
	}
	
	@Inject(method = "withdrawMana",
					at = @At("HEAD"),
					cancellable = true,
					locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					remap = false)
	private void withdrawManaLemma (int manaCost, boolean allowOvercast, CallbackInfoReturnable<Integer> cir) {
		if (manaCost <= 0) {
			cir.setReturnValue(0);
			return;
		}
		
//		HexalAPI.LOGGER.info("manaCost: %d".formatted(manaCost));

		MixinCastingContextInterface wispContext = (MixinCastingContextInterface)(Object)((CastingHarness)(Object)this).getCtx();
		
		BaseLemma wisp = wispContext.getLemma();
		
		if (wisp != null) {
			int mediaAvailable = wisp.getMedia();
//			HexalAPI.LOGGER.info("charging wisp %s for casting".formatted(wisp.getStringUUID()));
//			HexalAPI.LOGGER.info("mediaAvailable: %d".formatted(mediaAvailable));
//			HexalAPI.LOGGER.info("manaCost: %d".formatted(manaCost));
			int mediaToTake = Math.min(manaCost, mediaAvailable);
			manaCost -= mediaToTake;
			wisp.addMedia(-mediaToTake);
			cir.setReturnValue(manaCost);
		}
	}
}
