package ram.talia.hexal.fabric.client.blocks

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Vector3f
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer

class BlockEntityRelayRenderer(rendererProvider: BlockEntityRendererProvider.Context) : GeoBlockRenderer<BlockEntityRelay>(rendererProvider, BlockEntityRelayModel()) {

    override fun rotateBlock(facing: Direction, poseStack: PoseStack) = when (facing) {
            Direction.UP -> {
                poseStack.translate(0.0, -5/512.0, 0.0)
                poseStack.mulPose(Vector3f.XP.rotationDegrees(0f))
            }
            Direction.DOWN -> {
                poseStack.translate(0.0, 1.0-5/512.0, 0.0)
                poseStack.mulPose(Vector3f.XP.rotationDegrees(180f))
            }
            Direction.NORTH -> {
                poseStack.translate(0.0, 0.5-5/512.0, 0.5)
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90f))
            }
            Direction.SOUTH -> {
                poseStack.translate(0.0, 0.5-5/512.0, -0.5)
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90f))
            }
            Direction.EAST -> {
                poseStack.translate(-0.5, 0.5-5/512.0, 0.0)
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90f))
            }
            Direction.WEST -> {
                poseStack.translate(0.5, 0.5-5/512.0, 0.0)
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(90f))
            }
        }


    override fun getRenderType(animatable: BlockEntityRelay, partialTick: Float, poseStack: PoseStack,
                               bufferSource: MultiBufferSource?, buffer: VertexConsumer?, packedLight: Int,
                               texture: ResourceLocation): RenderType {
        return RenderType.entityTranslucent(getTextureLocation(animatable))
    }
}
