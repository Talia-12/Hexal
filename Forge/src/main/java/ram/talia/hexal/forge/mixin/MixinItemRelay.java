package ram.talia.hexal.forge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import ram.talia.hexal.common.items.ItemRelay;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

@Mixin(ItemRelay.class)
public abstract class MixinItemRelay implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        event.getController().setAnimation(
                RawAnimation.begin()
                        .then("animation.model.inv", Animation.LoopType.HOLD_ON_LAST_FRAME)
        );

        return PlayState.CONTINUE;
    }
}
