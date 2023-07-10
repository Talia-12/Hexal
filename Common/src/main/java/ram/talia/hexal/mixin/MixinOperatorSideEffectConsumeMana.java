package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static java.lang.Math.min;
import static ram.talia.hexal.common.casting.actions.spells.great.OpConsumeWisp.TAG_CONSUMED_MEDIA;

@Mixin(OperatorSideEffect.ConsumeMedia.class)
public abstract class MixinOperatorSideEffectConsumeMana {

	@Shadow @Final private int amount;

	private CastingVM harness;

	@Inject(method = "performEffect",
		at = @At(value = "HEAD"),
		locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false
	)
	private void performEffectWisp(CastingVM harness, CallbackInfoReturnable<Boolean> cir) {
			this.harness = harness;
		}

	/**
	 * Makes it so that if a wisp is consumed, that wisps media can be used later in the cast.
	 */
	@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
	@Redirect(method = "performEffect",
		at = @At(value = "FIELD", target = "Lat/petrak/hexcasting/api/casting/eval/sideeffects/OperatorSideEffect$ConsumeMedia;amount:I", opcode = 180), // GETFIELD
		remap = false)
	private int hexal$performEffect(OperatorSideEffect.ConsumeMedia media) {
		var image = harness.getImage();
		var userData = image.getUserData();
		if (!userData.contains(TAG_CONSUMED_MEDIA))
			return amount;

		var consumedMedia = userData.getLong(TAG_CONSUMED_MEDIA);
		var amountToUse = min(amount, consumedMedia);
		consumedMedia -= amountToUse;
		userData.putLong(TAG_CONSUMED_MEDIA, consumedMedia);

		return (int) (amount - amountToUse);
	}
}
