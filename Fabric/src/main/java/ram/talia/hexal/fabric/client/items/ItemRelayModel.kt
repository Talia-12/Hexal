package ram.talia.hexal.fabric.client.items

import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.items.ItemRelay
import software.bernie.geckolib3.model.AnimatedGeoModel

class ItemRelayModel : AnimatedGeoModel<ItemRelay>() {
    override fun getModelResource(relay: ItemRelay): ResourceLocation = HexalAPI.modLoc("geo/relay.geo.json")

    override fun getTextureResource(relay: ItemRelay): ResourceLocation = HexalAPI.modLoc("textures/block/relay.png")

    override fun getAnimationResource(relay: ItemRelay): ResourceLocation = HexalAPI.modLoc("animations/relay.animation.json")
}