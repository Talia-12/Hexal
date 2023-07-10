package ram.talia.hexal.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import ram.talia.hexal.common.items.ItemRelay;
import ram.talia.hexal.xplat.IXplatAbstractions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ItemRelay.class)
abstract class MixinItemRelay implements GeoItem {
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return instanceCache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(IXplatAbstractions.INSTANCE.getItemRelayRenderProvider());
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
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
