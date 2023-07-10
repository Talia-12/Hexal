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
    override fun registerControllers(p0: ControllerRegistrar?) {
        TODO("Not yet implemented")
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        TODO("Not yet implemented")
    }

    override fun createRenderer(consumer: Consumer<Any>?) {
        TODO("Not yet implemented")
    }

    override fun getRenderProvider(): Supplier<Any> {
        TODO("Not yet implemented")
    }
}