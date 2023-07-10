package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.casting.*;
import at.petrak.hexcasting.api.spell.casting.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.api.spell.iota.PatternIota;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.casting.wisp.IMixinCastingContext;
import ram.talia.hexal.common.lib.hex.HexActions;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.max;

@SuppressWarnings("ConstantConditions")
@Mixin(CastingHarness.class)
public abstract class MixinCastingHarness {
	private final CastingHarness harness = (CastingHarness) (Object) this;
	
	/**
	 * Makes it so that the wisp casting doesn't play particle effects around the player.
	 */
	@Redirect(method = "updateWithPattern",
			at = @At(
				value="INVOKE",
				target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
			),
			remap = false)
	private boolean updateWithPatternWisp (List<OperatorSideEffect> sideEffects, Object o) {

		if (o instanceof OperatorSideEffect.Particles particles) {

			CastingContext ctx = harness.getCtx();
			IMixinCastingContext ctxi = (IMixinCastingContext)(Object) ctx;

			if (!ctxi.hasWisp())
				return sideEffects.add(particles);
			else
				return false;
		}

		return sideEffects.add((OperatorSideEffect) o);
	}

	@SuppressWarnings("DefaultAnnotationParam")
	@Redirect(method = "executeIotas",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
					remap = true
			),
			remap = false)
	private void playSoundWisp (ServerLevel level, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float v, float p) {
		CastingContext ctx = harness.getCtx();
		IMixinCastingContext wispContext = (IMixinCastingContext) (Object) ctx;

		BaseCastingWisp wisp = wispContext.getWisp();

		if (wisp != null) {
			wisp.scheduleCastSound();
		} else {
			level.playSound(player, x, y, z, soundEvent, soundSource, v, p);
		}
	}

	/**
	 * Makes it so that a player/circle/whatever casting consume wisp grants that cast access to all the media from the
	 * consumed wisp that another wisp consuming it would get, to use in the next parts of the cast.
	 */
	@ModifyVariable(
			method = "withdrawMedia",
			at = @At("STORE"),
			ordinal = 0,
			remap = false,
			argsOnly = true
	)
	private int removeConsumedFromCost(int value) {
		CastingContext ctx = harness.getCtx();
		IMixinCastingContext iCtx = (IMixinCastingContext) (Object) ctx;

		var consumedMedia = iCtx.getConsumedMedia();
		if (consumedMedia == 0)
			return value;

		var newValue = max(value - consumedMedia, 0);
		consumedMedia -= (value - newValue);
		iCtx.setConsumedMedia(consumedMedia);

		return newValue;
	}
	
	/**
	 * Makes it so that the wisp casting draws its mana from the wisp rather than the player's inventory.
	 */
	@Inject(method = "withdrawMedia",
			at = @At("HEAD"),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			remap = false)
	private void withdrawMediaWisp(int manaCost, boolean allowOvercast, CallbackInfoReturnable<Integer> cir) {
		if (manaCost <= 0) {
			cir.setReturnValue(0);
			return;
		}
		
//		HexalAPI.LOGGER.info("manaCost: %d".formatted(manaCost));

		IMixinCastingContext wispContext = (IMixinCastingContext)(Object)((CastingHarness)(Object)this).getCtx();
		
		BaseCastingWisp wisp = wispContext.getWisp();
		
		if (wisp != null) {
			int mediaAvailable = wisp.getMedia();
			HexalAPI.LOGGER.debug("charging wisp %s for casting".formatted(wisp.getStringUUID()));
			HexalAPI.LOGGER.debug("mediaAvailable: %d".formatted(mediaAvailable));
			HexalAPI.LOGGER.debug("manaCost: %d".formatted(manaCost));
			int mediaToTake = Math.min(manaCost, mediaAvailable);
			manaCost -= mediaToTake;
			wisp.addMedia(-mediaToTake);
			cir.setReturnValue(manaCost);
		}
	}
	
	/**
	 * Has two functions. Firstly, makes it so that when a player executes a pattern, if that pattern is marked as a
	 * macro in their Everbook it executes the macro instead. Secondly, if the caster is transmitting to a Linkable it
	 * will send all iotas that would have been executed to the Linkable instead.
	 */
	@Inject(method = "executeIota",
			at = @At("HEAD"),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			remap = false)
	private void executeIotaMacro (Iota iota, ServerLevel world, CallbackInfoReturnable<ControllerInfo> cir) {
		CastingContext ctx = harness.getCtx();
		
		List<Iota> toExecute;
		
		// only work if the caster's enlightened, the caster is staff-casting, and they haven't escaped this pattern
		// (meaning you can get a copy of the pattern to mark it as not a macro again)
		if (ctx.getSpellCircle() != null || ((IMixinCastingContext) (Object) ctx).hasWisp())
			return;
		if (!ctx.isCasterEnlightened() || harness.getEscapeNext())
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else if (iota.getType() != HexIotaTypes.PATTERN
						 || ((PatternIota) iota).getPattern().sigsEqual(HexPattern.fromAngles("qqqaw", HexDir.EAST))) // hacky, make it so people can't lock themselves
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else {
			HexPattern pattern = ((PatternIota) iota).getPattern();
			toExecute = IXplatAbstractions.INSTANCE.getEverbookMacro(ctx.getCaster(), pattern);
			if (toExecute == null)
				toExecute = new ArrayList<>(Collections.singleton(iota));
		}
		
		// don't send unescaped escapes to the Linkable (lets you escape macros)
		// TODO: HACKYY
		boolean isUnescapedEscape = !harness.getEscapeNext() &&
				iota.getType() == HexIotaTypes.PATTERN &&
				((PatternIota) iota).getPattern().sigsEqual(HexPattern.fromAngles("qqqaw", HexDir.EAST));

		// sends the iotas straight to the Linkable that the player is forwarding iotas to, if it exists
		var transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(ctx.getCaster());
		boolean transmitting = transmittingTo != null;
		if (transmitting && !isUnescapedEscape) {
			var iter = toExecute.iterator();
			
			while (iter.hasNext()) {
				var it = iter.next();
				
				// if the current iota is an unescaped OpCloseTransmit, break so that Action can be processed by the player's handler.
				if (!harness.getEscapeNext() && iota.getType() == HexIotaTypes.PATTERN &&
						Iota.tolerates(iota, HexActions.LINK_COMM_CLOSE_TRANSMIT))
					break;
				
				iter.remove();
				transmittingTo.receiveIota(IXplatAbstractions.INSTANCE.getLinkstore(ctx.getCaster()), it);
			}
			
			harness.setEscapeNext(false);
		}
		
		boolean wasTransmitting = transmitting;
		// send all remaining iotas to the harness.
		var ret = harness.executeIotas(toExecute, world);
		transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(ctx.getCaster());
		transmitting = transmittingTo != null;
		boolean isEdgeTransmit = transmitting ^ wasTransmitting; // don't mark ESCAPED the opening and closing patterns.
		boolean isStackClear = ret.isStackClear() && !transmitting;
		ResolvedPatternType type = (transmitting && !isUnescapedEscape && !isEdgeTransmit) ? ResolvedPatternType.ESCAPED : ret.getResolutionType();
		List<CompoundTag> stack = transmitting ? transmittingTo.getAsActionResult().stream().map(HexIotaTypes::serialize).toList() : ret.getStack();
		List<CompoundTag> parenthesized = transmitting ? List.of() : ret.getParenthesized();
		CompoundTag ravenmind = transmitting ? null : ret.getRavenmind();
		int parenCount = transmitting ? 1 : ret.getParenCount();
		
		ret = ret.copy(isStackClear, type, stack, parenthesized, ravenmind, parenCount);

		cir.setReturnValue(ret);
	}
}
