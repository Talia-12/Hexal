package ram.talia.hexal.common.items

import net.minecraft.world.item.BlockItem
import ram.talia.hexal.common.lib.HexalBlocks
import ram.talia.hexal.xplat.IXplatAbstractions
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.core.animatable.GeoAnimatable
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar
import software.bernie.geckolib.core.animation.Animation
import software.bernie.geckolib.core.animation.AnimationController
import software.bernie.geckolib.core.animation.AnimationState
import software.bernie.geckolib.core.animation.RawAnimation
import software.bernie.geckolib.core.`object`.PlayState
import software.bernie.geckolib.util.GeckoLibUtil
import java.util.function.Consumer
import java.util.function.Supplier

class ItemRelay(properties: Properties) : BlockItem(HexalBlocks.RELAY, properties), GeoItem {

    //region GeoItem
    private val instanceCache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private val renderProvider: Supplier<Any> = GeoItem.makeRenderer(this)

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache = instanceCache

    override fun createRenderer(consumer: Consumer<Any>) {
        consumer.accept(IXplatAbstractions.INSTANCE.itemRelayRenderProvider)
    }

    override fun getRenderProvider(): Supplier<Any> = renderProvider


    override fun registerControllers(data: ControllerRegistrar) {
        data.add(AnimationController(this, "controller", 0, this::predicate))
    }



    private fun <E : GeoAnimatable> predicate(event: AnimationState<E>): PlayState {
        event.controller.setAnimation(
            RawAnimation.begin()
                .then("animation.model.inv", Animation.LoopType.HOLD_ON_LAST_FRAME)
        )

        return PlayState.CONTINUE
    }
    //endregion
}