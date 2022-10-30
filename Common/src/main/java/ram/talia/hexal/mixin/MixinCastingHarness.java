package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.spell.casting.*;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.spell.casting.IMixinCastingContext;
import ram.talia.hexal.common.casting.Patterns;
import ram.talia.hexal.common.casting.RegisterPatterns;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
@Mixin(CastingHarness.class)
public abstract class MixinCastingHarness {
	private final CastingHarness harness = (CastingHarness) (Object) this;
	
	@Shadow(remap = false) private boolean escapeNext;
	
	/**
	 * Makes it so that the wisp casting doesn't play side effects around the player.
	 */
	@Redirect(method = "updateWithPattern",
						at = @At(
									value="INVOKE",
									target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
					),
      remap = false)
	private boolean updateWithPatternWisp (List<OperatorSideEffect> sideEffects, Object o) {
		
		if (o instanceof OperatorSideEffect.Particles particles) {
			
			CastingContext ctx = ((CastingHarness)(Object)this).getCtx();
			IMixinCastingContext ctxi = (IMixinCastingContext)(Object) ctx;
			
			if (!ctxi.hasWisp())
				return sideEffects.add(particles);
			else
				return false;
		}
		
		return sideEffects.add((OperatorSideEffect) o);
	}
	
	/**
	 * Makes it so that the wisp casting draws its mana from the wisp rather than the player's inventory.
	 */
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
		
//		HexalAPI.LOGGER.info("manaCost: %d".formatted(manaCost));

		IMixinCastingContext wispContext = (IMixinCastingContext)(Object)((CastingHarness)(Object)this).getCtx();
		
		BaseCastingWisp wisp = wispContext.getWisp();
		
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
	
	/**
	 * Has two functions. Firstly, makes it so that when a player executes a pattern, if that pattern is marked as a macro in their Everbook it executes the macro instead.
	 * Secondly, if the caster is transmitting to a Linkable it will send all iotas that would have been executed to the Linkable instead.
	 */
	@Inject(method = "executeIota",
					at = @At("HEAD"),
					cancellable = true,
					locals = LocalCapture.CAPTURE_FAILEXCEPTION,
					remap = false)
	private void executeIotaMacro (SpellDatum<?> iota, ServerLevel world, CallbackInfoReturnable<ControllerInfo> cir) {
		CastingContext ctx = harness.getCtx();
		
		List<SpellDatum<?>> toExecute;
		
		// only work if the caster's enlightened, the caster is staff-casting, and they haven't escaped this pattern
		// (meaning you can get a copy of the pattern to mark it as not a macro again)
		if (ctx.getSpellCircle() != null || ((IMixinCastingContext) (Object) ctx).hasWisp())
			return;
		if (!ctx.isCasterEnlightened() || this.escapeNext)
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else if (iota.getType() != DatumType.PATTERN || ((HexPattern) iota.getPayload()).anglesSignature().equals("qqqaw")) // hacky, make it so people can't lock themselves
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else {
			HexPattern pattern = (HexPattern) iota.getPayload();
			toExecute = IXplatAbstractions.INSTANCE.getEverbookMacro(ctx.getCaster(), pattern);
			if (toExecute == null)
				toExecute = new ArrayList<>(Collections.singleton(iota));
		}
		
		// don't send unescaped escapes to the Linkable (lets you escape macros)
		// TODO: HACKYY
		boolean isUnescapedEscape = !this.escapeNext && iota.getType() == DatumType.PATTERN && ((HexPattern) iota.getPayload()).anglesSignature().equals("qqqaw");

		// sends the iotas straight to the Linkable that the player is forwarding iotas to, if it exists
		var transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(ctx.getCaster());
		boolean transmitting = transmittingTo != null;
		if (transmitting && !isUnescapedEscape) {
			var iter = toExecute.iterator();
			
			while (iter.hasNext()) {
				var it = iter.next();
				
				// if the current iota is an unescaped OpCloseTransmit, break so that Action can be processed by the player's handler.
				if (!this.escapeNext && it.getType() == DatumType.PATTERN &&
						((HexPattern) it.getPayload()).anglesSignature().equals(Patterns.LINK_COMM_CLOSE_TRANSMIT.getFirst().anglesSignature()))
					break;
				
				iter.remove();
				transmittingTo.receiveIota(it);
			}
			
			this.escapeNext = false;
		}
		
		boolean wasTransmitting = transmitting;
		// send all remaining iotas to the harness.
		var ret = harness.executeIotas(toExecute, world);
		transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(ctx.getCaster());
		transmitting = transmittingTo != null;
		boolean isEdgeTransmit = transmitting ^ wasTransmitting; // don't mark ESCAPED the opening and closing patterns.
		boolean isStackClear = ret.isStackClear() && !transmitting;
		ResolvedPatternType type = (transmitting && !isUnescapedEscape && !isEdgeTransmit) ? ResolvedPatternType.ESCAPED : ret.getResolutionType();
		List<Component> stackDesc = transmitting ? transmittingTo.transmittingTargetReturnDisplay() : ret.getStackDesc();
		
		ret = ret.copy(ret.getMakesCastSound(), isStackClear, type, stackDesc);

		cir.setReturnValue(ret);
	}
}
