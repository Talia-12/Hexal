package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface;
import ram.talia.hexal.common.entities.BaseWisp;

@Mixin(CastingHarness.class)
public abstract class MixinCastingHarness {
	
	@Inject(method = "withdrawMana",
					at = @At("HEAD"),
					cancellable = true,
					locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					remap = false)
	private void withdrawManaWisp (int manaCost, boolean allowOvercast, CallbackInfoReturnable<Integer> cir) {
		if (manaCost <= 0) {
			cir.setReturnValue(0);
			return;
		}
		
		HexalAPI.LOGGER.info("manaCost: %d".formatted(manaCost));

		MixinCastingContextInterface wispContext = (MixinCastingContextInterface)(Object)((CastingHarness)(Object)this).getCtx();
		
		BaseWisp wisp = wispContext.getWisp();
		
		if (wisp != null) {
			int mediaAvailable = wisp.getMedia();
			HexalAPI.LOGGER.info("charging wisp %s for casting".formatted(wisp.getStringUUID()));
			HexalAPI.LOGGER.info("mediaAvailable: %d".formatted(mediaAvailable));
			HexalAPI.LOGGER.info("manaCost: %d".formatted(manaCost));
			int mediaToTake = Math.min(manaCost, mediaAvailable);
			manaCost -= mediaToTake;
			wisp.addMedia(-mediaToTake);
			cir.setReturnValue(manaCost);
		}
	}
}
