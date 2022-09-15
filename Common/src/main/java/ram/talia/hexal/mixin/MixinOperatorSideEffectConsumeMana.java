package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.api.HexalAPI;
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface;
import ram.talia.hexal.common.entities.BaseCastingWisp;

@Mixin(OperatorSideEffect.ConsumeMana.class)
public abstract class MixinOperatorSideEffectConsumeMana {
	
	/**
	 * Makes it so that the cant_overcast message (i.e. message played when caster doesn't have enough media) doesn't play for cyclic wisps (and others that override
	 * {@link BaseCastingWisp#getShouldComplainNotEnoughMedia()}
	 */
	@Inject(method = "performEffect",
					at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendMessage(Lnet/minecraft/network/chat/Component;Ljava/util/UUID;)V"),
					cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false
	)
	private void performEffectWisp (CastingHarness harness, CallbackInfoReturnable<Boolean> cir, boolean overcastOk, int leftoverMana) {
		MixinCastingContextInterface ctxi = (MixinCastingContextInterface)(Object) harness.getCtx();
		
		HexalAPI.LOGGER.info("performEffectWisp called");
		
		if (ctxi.hasWisp()) {
			if (ctxi.getWisp().getShouldComplainNotEnoughMedia())
				harness.getCtx().getCaster().sendMessage(
								new TranslatableComponent("hexcasting.message.cant_overcast"),
								Util.NIL_UUID
				);
			
			cir.setReturnValue(leftoverMana > 0);
		}
	}
}
