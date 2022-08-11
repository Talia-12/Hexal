package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.casting.CastingContext;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.common.entities.BaseWisp;
import ram.talia.hexal.api.casting.MixinCastingContextInterface;

@Mixin(CastingContext.class)
public class MixinCastingContext implements MixinCastingContextInterface {
	private BaseWisp wisp;
	
	public BaseWisp getWisp () {
		return wisp;
	}
	
	public BaseWisp setWisp (BaseWisp wisp) {
		this.wisp = wisp;
		return this.wisp;
	}
	
	@Inject(method = "isVecInRange", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					slice = @Slice(
						from = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"),
						to = @At("TAIL")
					))
	private void isVecInRangeWisp (Vec3 vec, CallbackInfoReturnable<Boolean> cir) {
		if (this.wisp != null) {
			cir.setReturnValue(vec.distanceToSqr(this.wisp.position()) < BaseWisp.MAX_DISTANCE_TO_WISP * BaseWisp.MAX_DISTANCE_TO_WISP);
		}
	}
}
