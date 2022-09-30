package ram.talia.hexal.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ram.talia.hexal.fabric.events.Events;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	
	@Shadow @Nullable public LocalPlayer player;
	
	@Inject(method = "clearLevel()V", at = @At("HEAD"))
	private void clearLevelOnClientLogoutEvent (CallbackInfo ci) {
		Events.CLIENT_LOGGOUT.invoker().onClientLogout(this.player);
	}
}
