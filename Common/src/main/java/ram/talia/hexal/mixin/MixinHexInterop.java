package ram.talia.hexal.mixin;

import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.api.PatchouliAPI;

@Mixin(HexInterop.class)
public abstract class MixinHexInterop {
    @Inject(
            method = "initPatchouli",
            at = @At("RETURN"),
            remap = false
    )
    private static void hexal$forceInitIfFabric(CallbackInfo ci) {
        if (IXplatAbstractions.INSTANCE.platform() == Platform.FABRIC)
            PatchouliAPI.get().setConfigFlag(HexInterop.PATCHOULI_ANY_INTEROP_FLAG, true);
    }
}
