package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface;

/**
 * Modifies {@link at.petrak.hexcasting.api.spell.casting.CastingContext} to make it properly allow wisps to affect things within their range.
 */
@Mixin(CastingContext.class)
public abstract class MixinCastingContext implements MixinCastingContextInterface {
	private BaseCastingWisp wisp;
	
	@Shadow private int depth;
	
	public BaseCastingWisp getWisp () {
		return wisp;
	}
	
	public BaseCastingWisp setWisp (BaseCastingWisp wisp) {
		this.wisp = wisp;
		return this.wisp;
	}
	
	public boolean hasWisp () {
		return wisp != null;
	}
	
	/**
	 * Returns the remaining evals the context can perform (mixin used since {@link at.petrak.hexcasting.api.spell.casting.CastingContext}.depth is private)
	 */
	public int remainingDepth () {
		return HexConfig.server().maxRecurseDepth() - this.depth;
	}
	
	/**
	 * Modifies {@link at.petrak.hexcasting.api.spell.casting.CastingContext} to make it properly allow wisps to affect things within their range. The INVOKE location and
	 * use of cancellable mean the wisp can affect things in range of the player's greater sentinel, but can't affect things in range of the player.
	 */
	@Inject(method = "isVecInRange", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					slice = @Slice(
						from = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 2),
						to = @At("TAIL")
					))
	private void isVecInRangeWisp (Vec3 vec, CallbackInfoReturnable<Boolean> cir) {
		if (this.wisp != null) {
			cir.setReturnValue(vec.distanceToSqr(this.wisp.position()) < this.wisp.maxSqrCastingDistance());
		}
	}
}
