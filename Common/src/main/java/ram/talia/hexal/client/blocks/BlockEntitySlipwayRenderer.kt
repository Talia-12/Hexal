package ram.talia.hexal.client.blocks

import at.petrak.hexcasting.client.drawLineSeq
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec2
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway

class BlockEntitySlipwayRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<BlockEntitySlipway> {

	private val bDispatcher: BlockRenderDispatcher
	private val bEDispatcher: BlockEntityRenderDispatcher
	private val iRenderer: ItemRenderer

	init {
		bDispatcher = ctx.blockRenderDispatcher
		bEDispatcher = ctx.blockEntityRenderDispatcher
		iRenderer = Minecraft.getInstance().itemRenderer
	}

	override fun render(slipway: BlockEntitySlipway, tickDelta: Float, ps: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
		ps.pushPose()

		bDispatcher.renderSingleBlock(Blocks.GLASS.defaultBlockState(), ps, buffer, light, overlay)

//		drawLineSeq(ps.last().pose(), listOf(Vec2(0f, 0f), Vec2(1f, 1f)), 0.4f, 0f, -0x9b3701, -0x9b3701)

		ps.popPose()
	}
}