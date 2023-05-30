package ram.talia.hexal.fabric.client.blocks

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import software.bernie.geckolib3.model.AnimatedGeoModel
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer

class BlockEntityRelayRenderer(modelProvider: AnimatedGeoModel<BlockEntityRelay>) : GeoBlockRenderer<BlockEntityRelay>(modelProvider) {

    override fun getRenderType(animatable: BlockEntityRelay, partialTick: Float, poseStack: PoseStack,
                               bufferSource: MultiBufferSource?, buffer: VertexConsumer?, packedLight: Int,
                               texture: ResourceLocation): RenderType {
        return RenderType.entityTranslucent(getTextureLocation(animatable))
    }
}
