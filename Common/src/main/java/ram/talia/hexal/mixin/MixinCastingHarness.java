package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.ControllerInfo;
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.linkable.ILinkable;
import ram.talia.hexal.api.spell.casting.IMixinCastingContext;
import ram.talia.hexal.common.casting.actions.spells.link.OpCloseTransmit;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
@Mixin(CastingHarness.class)
public abstract class MixinCastingHarness {
	private final CastingHarness harness = (CastingHarness) (Object) this;
	
	@Shadow private boolean escapeNext;
	
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
	 * Has two functions. Firstly, makes it so that when a player executes a pattern, if that pattern is marked as a macro in their Everbook it executes the macro instead. Secondly, if the caster is
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
		else if (iota.getType() != DatumType.PATTERN)
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else {
			HexPattern pattern = (HexPattern) iota.getPayload();
			toExecute = IXplatAbstractions.INSTANCE.getEverbookMacro(ctx.getCaster(), pattern);
			if (toExecute == null)
				toExecute = new ArrayList<>(Collections.singleton(iota));
		}
		
		// sends the iotas straight to the Linkable that the player is forwarding iotas to, if it exists
		var transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(ctx.getCaster());
		if (transmittingTo != null) {
			var iter = toExecute.iterator();
			
			while (iter.hasNext()) {
				var it = iter.next();
				
				// if the current iota is an OpCloseTransmit, break so that Action can be processed by the player's handler.
				if (it.getType() == DatumType.PATTERN && it.getPayload().equals(OpCloseTransmit.PATTERN))
					break;
				
				iter.remove();
				transmittingTo.receiveIota(it);
			}
		}
		
		// send all remaining iotas to the harness.
		var ret = harness.executeIotas(toExecute, world);

		cir.setReturnValue(ret);
	}
}
