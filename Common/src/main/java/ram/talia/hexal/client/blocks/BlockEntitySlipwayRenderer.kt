package ram.talia.hexal.client.blocks

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.BlockRenderDispatcher
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.nextColour
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway
import java.util.*

class BlockEntitySlipwayRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<BlockEntitySlipway> {

	private val random = RandomSource.create() // a bit cursed, not sure how I'm actually meant to do this

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

//		bDispatcher.renderSingleBlock(Blocks.GLASS.defaultBlockState(), ps, buffer, light, overlay)

		val level = slipway.level

		if (level == null) {
			ps.popPose()
			return
		}

		val pos = Vec3.atCenterOf(slipway.pos)

		for (colouriser in HexItems.DYE_COLORIZERS.values) {
			val frozenColouriser = FrozenColorizer(ItemStack(colouriser), Util.NIL_UUID)
			val colour: Int = frozenColouriser.nextColour(random)

			level.addParticle(
				ConjureParticleOptions(colour, true),
				(pos.x + RADIUS*random.nextGaussian()),
				(pos.y + RADIUS*random.nextGaussian()),
				(pos.z + RADIUS*random.nextGaussian()),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
//		drawLineSeq(ps.last().pose(), listOf(Vec2(0f, 0f), Vec2(1f, 1f)), 0.4f, 0f, -0x9b3701, -0x9b3701)

		ps.popPose()
	}

	companion object {
		const val RADIUS = 0.25
	}
}