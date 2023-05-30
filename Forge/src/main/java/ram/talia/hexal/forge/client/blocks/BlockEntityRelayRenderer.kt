package ram.talia.hexal.fabric.client.blocks

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer

class BlockEntityRelayRenderer(rendererProvider: BlockEntityRendererProvider.Context) : GeoBlockRenderer<BlockEntityRelay>(rendererProvider, BlockEntityRelayModel()) {

    override fun getRenderType(animatable: BlockEntityRelay, partialTick: Float, poseStack: PoseStack,
                               bufferSource: MultiBufferSource?, buffer: VertexConsumer?, packedLight: Int,
                               texture: ResourceLocation): RenderType {
        return RenderType.entityTranslucent(getTextureLocation(animatable))
    }
}
