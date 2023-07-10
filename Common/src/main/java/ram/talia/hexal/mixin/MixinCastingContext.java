package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.api.casting.wisp.IMixinCastingContext;
import ram.talia.hexal.common.entities.BaseCastingWisp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.max;

/**
 * Modifies {@link at.petrak.hexcasting.api.spell.casting.CastingContext} to make it properly allow wisps to affect things within their range.
 */
@Mixin(CastingContext.class)
public abstract class MixinCastingContext implements IMixinCastingContext {
	private final CastingContext self = (CastingContext) (Object) this;

	private BaseCastingWisp wisp;

	private int consumedMedia;
	private final Map<BlockPos, Integer> numTimesTicked = new HashMap<>(); // stores how many times each blockpos has been ticked by OpTick (tick acceleration). Used to compute cost.

	private @Nullable UUID temporaryStorage; // UUID of the temporary Mediafied Item Storage that is being used by this ctx, if it exists.

	@Override
	public @Nullable UUID getBoundStorage() {
		if (temporaryStorage != null)
			return temporaryStorage;
		return MediafiedItemManager.getBoundStorage(self.getCaster());
	}

	@Override
	public void setTemporaryBoundStorage(@Nullable UUID temporaryStorage) {
		this.temporaryStorage = temporaryStorage;
	}

	@Override
	public int getConsumedMedia() {
		return consumedMedia;
	}

	@Override
	public void setConsumedMedia(int media) {
		consumedMedia = max(media, 0);
	}

	@Override
	public int getTimesTicked(BlockPos pos) {
		return numTimesTicked.getOrDefault(pos, 0);
	}

	@Override
	public void incTimesTicked(BlockPos pos) {
		numTimesTicked.merge(pos, 1, Integer::sum);
	}
	
	@Shadow(remap = false) private int depth;
	
	public @Nullable BaseCastingWisp getWisp () {
		return wisp;
	}
	
	public @Nullable BaseCastingWisp setWisp (@Nullable BaseCastingWisp wisp) {
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
	@Inject(method = "isVecInRange", at = @At(value = "RETURN", ordinal = 3), cancellable = true,
			locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false)
	private void isVecInRangeWisp (Vec3 vec, CallbackInfoReturnable<Boolean> cir) {
		if (this.wisp != null) {
			cir.setReturnValue(vec.distanceToSqr(this.wisp.position()) <= this.wisp.maxSqrCastingDistance());
		}
	}
}
