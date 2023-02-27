package ram.talia.hexal.client.blocks

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage.AnimationState

class BlockEntityMediafiedStorageRenderer : BlockEntityRenderer<BlockEntityMediafiedStorage> {
    val model = null

    override fun render(
        blockEntity: BlockEntityMediafiedStorage,
        tickDelta: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val totalTime = (blockEntity.level?.gameTime ?: 0) + tickDelta

        //no cull to show faces from inside
        val buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE_LOCATION))

        poseStack.pushPose()
        val entry = poseStack.last()
        val poseMatrix = entry.pose()
        val normalMatrix = entry.normal()

        //define the drawing utils
        fun vertex(x: Float, y: Float, z: Float, normX: Float, normY: Float, normZ: Float, u: Float, v: Float) {
            buffer
                .vertex(poseMatrix, x, y, z)
                .color(0xFFFFFFFFu.toInt())
                .uv(u / TEXTURE_SIZE, v / TEXTURE_SIZE)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normalMatrix, normX, normY, normZ)
                .endVertex()
        }

        fun drawHorizontalQuad(radius: Float, u: Float, v: Float, yNormal: Float) {
            val diameter = radius * 2

            vertex(-radius, 0f, -radius, 0f, yNormal, 0f, u, v)
            vertex(-radius, 0f, radius, 0f, yNormal, 0f, u, v + diameter)
            vertex(radius, 0f, radius, 0f, yNormal, 0f, u + diameter, v + diameter)
            vertex(radius, 0f, -radius, 0f, yNormal, 0f, u + diameter, v)
        }

        fun drawWalls(v: Float) {
            repeat(4) {
                val uStart = 16f * it

                vertex(-8f, WALL_HEIGHT, -8f, 0f, 0f, -1f, uStart, v)
                vertex(8f, WALL_HEIGHT, -8f, 0f, 0f, -1f, uStart + 16f, v)
                vertex(8f, 0f, -8f, 0f, 0f, -1f, uStart + 16f, v + WALL_HEIGHT)
                vertex(-8f, 0f, -8f, 0f, 0f, -1f, uStart, v + WALL_HEIGHT)

                poseStack.mulPose(Vector3f.YP.rotationDegrees(90f)) //rotate to next wall
            }
        }

        //switch to block space with 0 at center of bottom face
        poseStack.translate(0.5, 0.0, 0.5)
        poseStack.scale(1 / 16f, 1 / 16f, 1 / 16f)

        //z-fighting, more like
        poseStack.translate(0.0, 0.001, 0.0)
        poseStack.scale(0.999f, 0.999f, 0.999f)

        //bottom box half
        drawHorizontalQuad(8f, BOTTOM_U, BOTTOM_V, -1f)
        drawWalls(LOWER_WALL_V)


        val animationProgress = Mth.clamp(
            (blockEntity.currentAnimation.progress + tickDelta) / BlockEntityMediafiedStorage.ANIMATION_LENGTH,
            0f, 1f
        )

        //calculate lid position and circle scaling from easing
        val (height, angle, circleScale) = if (blockEntity.currentAnimation is AnimationState.Closing) {
            Triple(
                closingHeightEasing(Mth.lerp(animationProgress.toDouble(), OPEN_LID_HEIGHT, CLOSED_LID_HEIGHT)),
                closingAngleEasing(Mth.lerp(animationProgress, OPEN_LID_ANGLE, CLOSED_LID_ANGLE)),
                1 - animationProgress
            )
        } else {
            Triple(
                openingHeightEasing(Mth.lerp(animationProgress.toDouble(), CLOSED_LID_HEIGHT, OPEN_LID_HEIGHT)),
                openingAngleEasing(Mth.lerp(animationProgress, CLOSED_LID_ANGLE, OPEN_LID_ANGLE)),
                animationProgress
            )
        }

        //circle
        if (circleScale > 0.01) {
            val circleRotation = (totalTime * SPINS_PER_SECOND * 18) % 360f

            poseStack.translate(0.0, 8.0, 0.0)
            poseStack.mulPose(Vector3f.YP.rotationDegrees(circleRotation))
            poseStack.scale(circleScale, circleScale, circleScale)

            drawHorizontalQuad(CIRCLE_RADIUS, CIRCLE_U, CIRCLE_V, 1f)

            poseStack.scale(1 / circleScale, 1 / circleScale, 1 / circleScale)
            poseStack.mulPose(Vector3f.YN.rotationDegrees(circleRotation))
            poseStack.translate(0.0, -8.0, 0.0)
        }


        //top box half
        poseStack.translate(0.0, height, 0.0)
        poseStack.mulPose(Vector3f.YP.rotationDegrees(angle))
        drawWalls(UPPER_WALL_V)
        poseStack.translate(0.0, WALL_HEIGHT.toDouble(), 0.0)
        drawHorizontalQuad(8f, TOP_U, TOP_V, 1f)

        poseStack.popPose()
    }

    private fun openingHeightEasing(progress: Double): Double {
        return progress
    }

    private fun openingAngleEasing(progress: Float): Float {
        return progress
    }

    private fun closingHeightEasing(progress: Double): Double {
        return progress
    }

    private fun closingAngleEasing(progress: Float): Float {
        return progress
    }

    companion object {
        val TEXTURE_LOCATION: ResourceLocation = HexalAPI.modLoc("textures/block/mediafied_storage.png")
        const val TEXTURE_SIZE = 64f

        const val UPPER_WALL_V = 16f
        const val LOWER_WALL_V = 26f

        const val BOTTOM_U = 0f
        const val BOTTOM_V = 0f

        const val TOP_U = 0f
        const val TOP_V = 0f

        const val CIRCLE_U = 0f
        const val CIRCLE_V = 32f

        const val CIRCLE_RADIUS = 10f

        const val WALL_HEIGHT = 6f

        const val SPINS_PER_SECOND = 0.5f

        const val CLOSED_LID_HEIGHT = 4.0
        const val CLOSED_LID_ANGLE = 0f

        const val OPEN_LID_HEIGHT = 16.0 - WALL_HEIGHT
        const val OPEN_LID_ANGLE = 90f
    }
}
