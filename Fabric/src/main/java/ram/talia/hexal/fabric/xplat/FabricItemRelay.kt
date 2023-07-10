package ram.talia.hexal.fabric.xplat

import ram.talia.hexal.common.items.ItemRelay
import ram.talia.hexal.xplat.IXplatAbstractions
import software.bernie.geckolib.animatable.GeoItem
import java.util.function.Consumer
import java.util.function.Supplier

class FabricItemRelay(properties: Properties) : ItemRelay(properties), GeoItem {
    private val renderProvider = GeoItem.makeRenderer(this)

    override fun createRenderer(consumer: Consumer<Any>) {
        consumer.accept(IXplatAbstractions.INSTANCE.itemRelayRenderProvider)
    }

    override fun getRenderProvider(): Supplier<Any> {
        return renderProvider
    }
}