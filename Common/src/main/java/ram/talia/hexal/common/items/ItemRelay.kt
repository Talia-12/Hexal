package ram.talia.hexal.common.items

import net.minecraft.world.item.BlockItem
import ram.talia.hexal.common.lib.HexalBlocks
import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.builder.AnimationBuilder
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.event.predicate.AnimationEvent
import software.bernie.geckolib3.core.manager.AnimationData
import software.bernie.geckolib3.core.manager.AnimationFactory

class ItemRelay(properties: Properties) : BlockItem(HexalBlocks.RELAY, properties), IAnimatable {

    //region IAnimatable
    @Suppress("DEPRECATION", "removal")
    private val factory: AnimationFactory = AnimationFactory(this)

    override fun registerControllers(data: AnimationData) {
        data.addAnimationController(AnimationController(this, "controller", 0.0f, this::predicate))
    }

    private fun <E : IAnimatable> predicate(event: AnimationEvent<E>): PlayState {
        @Suppress("DEPRECATION", "removal")
        event.controller.setAnimation(
            AnimationBuilder()
                .addAnimation("animation.model.inv", false)
        )

        return PlayState.CONTINUE
    }

    override fun getFactory(): AnimationFactory = factory
    //endregion
}