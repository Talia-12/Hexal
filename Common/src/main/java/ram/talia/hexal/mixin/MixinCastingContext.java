package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.casting.CastingContext;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.common.entities.BaseLemma;
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface;

/**
 * Modifies [at.petrak.hexcasting.api.spell.casting.CastingContext] to make it properly allow lemmas to affect things within their range.
 */
@Mixin(CastingContext.class)
public abstract class MixinCastingContext implements MixinCastingContextInterface {
	private BaseLemma lemma;
	
	public BaseLemma getLemma () {
		return lemma;
	}
	
	public BaseLemma setLemma (BaseLemma lemma) {
		this.lemma = lemma;
		return this.lemma;
	}
	
	public boolean hasLemma () {
		return lemma != null;
	}
	
	/**
	 * Modifies [at.petrak.hexcasting.api.spell.casting.CastingContext] to make it properly allow lemmas to affect things within their range.
	 */
	@Inject(method = "isVecInRange", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					slice = @Slice(
						from = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"),
						to = @At("TAIL")
					))
	private void isVecInRangeLemma (Vec3 vec, CallbackInfoReturnable<Boolean> cir) {
		if (this.lemma != null) {
			cir.setReturnValue(vec.distanceToSqr(this.lemma.position()) < this.lemma.maxSqrCastingDistance());
		}
	}
}
