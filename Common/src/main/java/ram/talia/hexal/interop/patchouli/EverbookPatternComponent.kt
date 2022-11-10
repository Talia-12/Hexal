package ram.talia.hexal.interop.patchouli

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.lib.HexIotaTypes
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.datafixers.util.Pair
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import ram.talia.hexal.xplat.IClientXplatAbstractions
import vazkii.patchouli.api.IComponentRenderContext
import vazkii.patchouli.api.IVariable
import vazkii.patchouli.client.book.gui.GuiBook
import java.util.function.UnaryOperator

@Suppress("SameParameterValue", "unused")
class EverbookPatternComponent : AbstractPatternComponent() {
	@Transient
	var indexNum: Int = -1
	@Transient
	var isMacro = false
	@Transient
	var iota: CompoundTag? = null

	override fun build(x: Int, y: Int, pagenum: Int) {
		super.build(x, if (y != -1 && y != 70) { y } else { 50 }, pagenum)
		indexNum = pagenum - 1
	}

	override fun getPatterns(lookup: UnaryOperator<IVariable>): List<Pair<HexPattern, HexCoord>> {
		val pattern = IClientXplatAbstractions.INSTANCE.getClientEverbookPattern(indexNum) ?: return listOf()

		isMacro = IClientXplatAbstractions.INSTANCE.isClientEverbookMacro(pattern)
		iota = IClientXplatAbstractions.INSTANCE.getClientEverbookIota(pattern)

		return listOf(Pair(pattern, HexCoord.Origin))
	}

	override fun onDisplayed(context: IComponentRenderContext) {
		onVariablesAvailable { it }
	}

	override fun render(poseStack: PoseStack, ctx: IComponentRenderContext, partialTicks: Float, mouseX: Int, mouseY: Int) {
		poseStack.pushPose()
		poseStack.translate(HEADER_X.toDouble(), HEADER_Y.toDouble(), 0.0)

		val headerComponent = (if (isMacro) "hexal.everbook_pattern_entry.macro_header" else "hexal.everbook_pattern_entry.header").asTranslatedComponent(indexNum)

		drawCenteredStringNoShadow(poseStack, headerComponent, 0, 0, 0)
		poseStack.popPose()

		drawWrappedIota(poseStack, iota, DATA_X, DATA_Y, 0)

		super.render(poseStack, ctx, partialTicks, mouseX, mouseY)
	}

	override fun showStrokeOrder() = true

	private fun drawCenteredStringNoShadow(ms: PoseStack, s: Component, x: Int, y: Int, colour: Int) {
		val font = Minecraft.getInstance().font
		font.draw(ms, s, x - font.width(s) / 2.0f, y.toFloat(), colour)
	}

	private fun drawWrappedIota(ms: PoseStack, iota: CompoundTag?, x: Int, y: Int, colour: Int) {
		if (iota == null)
			return

		val font = Minecraft.getInstance().font

		val iotaText = HexIotaTypes.getDisplayWithMaxWidth(iota, GuiBook.PAGE_WIDTH, font).iterator()

		var currentY = y

		while (iotaText.hasNext() && currentY <= y + 5 * 9) { // don't draw more lines than fit in the book.
			ms.pushPose()
			ms.translate(x.toDouble(), currentY.toDouble(), 0.0)
			val toDraw = if (currentY < y + 5 * 9) { iotaText.next() } else { "...".red.visualOrderText }
			font.draw(ms, toDraw, 0.0f, 0.0f, colour)
			ms.popPose()
			currentY += 9
		}
	}

	companion object {
		const val HEADER_X = GuiBook.PAGE_WIDTH / 2
		const val HEADER_Y = 0
		const val DATA_X = 0
		const val DATA_Y = 100
	}
}