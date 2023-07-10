package ram.talia.hexal.fabric.client.blocks

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay
import software.bernie.geckolib.renderer.GeoBlockRenderer

class BlockEntityRelayRenderer : GeoBlockRenderer<BlockEntityRelay>(BlockEntityRelayModel()) {

    override fun rotateBlock(facing: Direction, poseStack: PoseStack) = when (facing) {
        Direction.UP -> {
            poseStack.translate(0.0, -5/512.0, 0.0)
            poseStack.mulPose(Axis.XP.rotationDegrees(0f))
        }
        Direction.DOWN -> {
            poseStack.translate(0.0, 1.0-5/512.0, 0.0)
            poseStack.mulPose(Axis.XP.rotationDegrees(180f))
        }
        Direction.NORTH -> {
            poseStack.translate(0.0, 0.5-5/512.0, 0.5)
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f))
        }
        Direction.SOUTH -> {
            poseStack.translate(0.0, 0.5-5/512.0, -0.5)
            poseStack.mulPose(Axis.XP.rotationDegrees(90f))
        }
        Direction.EAST -> {
            poseStack.translate(-0.5, 0.5-5/512.0, 0.0)
            poseStack.mulPose(Axis.ZP.rotationDegrees(-90f))
        }
        Direction.WEST -> {
            poseStack.translate(0.5, 0.5-5/512.0, 0.0)
            poseStack.mulPose(Axis.ZP.rotationDegrees(90f))
        }
    }


    override fun getRenderType(animatable: BlockEntityRelay, texture: ResourceLocation, bufferSource: MultiBufferSource?, partialTick: Float): RenderType {
        return RenderType.entityTranslucent(getTextureLocation(animatable))
    }
}
