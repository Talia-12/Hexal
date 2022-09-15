package ram.talia.hexal.mixin;

import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ram.talia.hexal.api.fakes.FakePlayer;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements {
	
	@Shadow private ServerPlayer player;
	
	/**
	 * Force the PlayerAdvancements to not load if the player that it was passed is a fake player.
	 */
	@Inject(method = "load", at = @At("HEAD"), cancellable = true)
	private void loadFakePlayer(ServerAdvancementManager $$0, CallbackInfo ci) {
		if (this.player instanceof FakePlayer)
			ci.cancel();
	}
}
