package ram.talia.hexal.client.blocks

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import ram.talia.hexal.common.blocks.entity.BlockEntityRelay

class BlockEntityRelayRenderer : BlockEntityRenderer<BlockEntityRelay> {
    override fun render(
        blockEntity: BlockEntityRelay,
        tickDelta: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
    }
}
