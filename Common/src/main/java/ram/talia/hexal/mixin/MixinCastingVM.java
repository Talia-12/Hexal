package ram.talia.hexal.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ram.talia.hexal.common.lib.hex.HexalActions;
import ram.talia.hexal.xplat.IXplatAbstractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
@Mixin(CastingVM.class)
public abstract class MixinCastingVM {
	private final CastingVM harness = (CastingVM) (Object) this;
	
	/**
	 * Has two functions. Firstly, makes it so that when a player executes a pattern, if that pattern is marked as a
	 * macro in their Everbook it executes the macro instead. Secondly, if the caster is transmitting to a Linkable it
	 * will send all iotas that would have been executed to the Linkable instead.
	 */
	@Inject(method = "queueExecuteAndWrapIota",
			at = @At("HEAD"),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILEXCEPTION,
			remap = false)
	private void executeIotaMacro (Iota iota, ServerLevel world, CallbackInfoReturnable<ExecutionClientView> cir) {
		CastingEnvironment env = harness.getEnv();
		var image = harness.getImage();

		var escapeNext = image.getEscapeNext();
		
		List<Iota> toExecute;
		
		// only work if the caster's enlightened, the caster is staff-casting, and they haven't escaped this pattern
		// (meaning you can get a copy of the pattern to mark it as not a macro again)
		if (!(env instanceof StaffCastEnv))
			return;
		if (escapeNext) // TODO: Add back in check if caster is enlightened
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else if (iota.getType() != HexIotaTypes.PATTERN
						 || ((PatternIota) iota).getPattern().sigsEqual(HexPattern.fromAngles("qqqaw", HexDir.EAST))) // hacky, make it so people can't lock themselves
			toExecute = new ArrayList<>(Collections.singleton(iota));
		else {
			HexPattern pattern = ((PatternIota) iota).getPattern();
			toExecute = IXplatAbstractions.INSTANCE.getEverbookMacro(env.getCaster(), pattern);
			if (toExecute == null)
				toExecute = new ArrayList<>(Collections.singleton(iota));
		}

		// don't send unescaped escapes to the Linkable (lets you escape macros)
		// TODO: HACKYY
		boolean isUnescapedEscape = !escapeNext &&
				iota.getType() == HexIotaTypes.PATTERN &&
				((PatternIota) iota).getPattern().sigsEqual(HexPattern.fromAngles("qqqaw", HexDir.EAST));

		// sends the iotas straight to the Linkable that the player is forwarding iotas to, if it exists
		var transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(env.getCaster());
		boolean transmitting = transmittingTo != null;
		if (transmitting && !isUnescapedEscape) {
			var iter = toExecute.iterator();
			
			while (iter.hasNext()) {
				var it = iter.next();
				
				// if the current iota is an unescaped OpCloseTransmit, break so that Action can be processed by the player's handler.
				if (!escapeNext && iota.getType() == HexIotaTypes.PATTERN &&
						Iota.tolerates(iota, new PatternIota(HexalActions.LINK_COMM_CLOSE_TRANSMIT.prototype())))
					break;
				
				iter.remove();
				transmittingTo.receiveIota(IXplatAbstractions.INSTANCE.getLinkstore(env.getCaster()), it);
			}

			harness.setImage(image.copy(image.getStack(), image.getParenCount(), image.getParenthesized(), false, image.getOpsConsumed(), image.getUserData()));
		}
		
		boolean wasTransmitting = transmitting;
		// send all remaining iotas to the harness.
		var ret = harness.queueExecuteAndWrapIotas(toExecute, world);
		transmittingTo = IXplatAbstractions.INSTANCE.getPlayerTransmittingTo(env.getCaster());
		transmitting = transmittingTo != null;
		boolean isEdgeTransmit = transmitting ^ wasTransmitting; // don't mark ESCAPED the opening and closing patterns.
		boolean isStackClear = ret.isStackClear() && !transmitting;
		ResolvedPatternType type = (transmitting && !isUnescapedEscape && !isEdgeTransmit) ? ResolvedPatternType.ESCAPED : ret.getResolutionType();
		List<CompoundTag> stack = transmitting ? transmittingTo.getAsActionResult().stream().map(IotaType::serialize).toList() : ret.getStackDescs();
		CompoundTag ravenmind = transmitting ? null : ret.getRavenmind();

		ret = ret.copy(isStackClear, type, stack, ravenmind);

		cir.setReturnValue(ret);
	}
}
