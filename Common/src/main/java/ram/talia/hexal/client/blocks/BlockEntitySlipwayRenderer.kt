package ram.talia.hexal.client.blocks

import at.petrak.hexcasting.client.drawLineSeq
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.phys.Vec2
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway

class BlockEntitySlipwayRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<BlockEntitySlipway> {

	override fun render(slipway: BlockEntitySlipway, tickDelta: Float, ps: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
		ps.pushPose()

		HexalAPI.LOGGER.info("rendering slipway at ${slipway.pos}")
		drawLineSeq(ps.last().pose(), listOf(Vec2(0f, 0f), Vec2(1f, 1f)), 0.4f, 0f, -0x9b3701, -0x9b3701)

		ps.popPose()
	}
}