package ram.talia.hexal.interop.patchouli

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.datafixers.util.Pair
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.TranslatableComponent
import ram.talia.hexal.xplat.IClientXplatAbstractions
import vazkii.patchouli.api.IComponentRenderContext
import vazkii.patchouli.api.IVariable
import vazkii.patchouli.client.book.gui.GuiBook
import java.util.function.UnaryOperator

@Suppress("SameParameterValue")
class EverbookPatternComponent : AbstractPatternComponent() {
	@Transient
	var indexNum: Int = -1
	@Transient
	var isMacro = false

	override fun build(x: Int, y: Int, pagenum: Int) {
		super.build(x, y, pagenum)
		indexNum = pagenum - 1
	}

	override fun getPatterns(lookup: UnaryOperator<IVariable>): List<Pair<HexPattern, HexCoord>> {
		val pattern: HexPattern = IClientXplatAbstractions.INSTANCE.getClientEverbookPattern(indexNum) ?: return listOf()

		isMacro = IClientXplatAbstractions.INSTANCE.isClientEverbookMacro(pattern)

		return listOf(Pair(pattern, HexCoord.Origin))
	}

	override fun onDisplayed(context: IComponentRenderContext) {
		onVariablesAvailable { it }
	}

	override fun render(poseStack: PoseStack, ctx: IComponentRenderContext, partialTicks: Float, mouseX: Int, mouseY: Int) {
		poseStack.pushPose()
		poseStack.translate(HEADER_X.toDouble(), HEADER_Y.toDouble(), 0.0)

		val component = (if (isMacro) "hexal.everbook_pattern_entry.macro_header" else "hexal.everbook_pattern_entry.header").asTranslatedComponent(indexNum)

		drawCenteredStringNoShadow(poseStack, component, 0, 0, 0)

		poseStack.popPose()

		super.render(poseStack, ctx, partialTicks, mouseX, mouseY)
	}

	override fun showStrokeOrder() = true

	private fun drawCenteredStringNoShadow(ms: PoseStack, s: TranslatableComponent, x: Int, y: Int, color: Int) {
		val font = Minecraft.getInstance().font
		font.draw(ms, s, x - font.width(s) / 2.0f, y.toFloat(), color)
	}

	companion object {
		const val HEADER_X = GuiBook.PAGE_WIDTH / 2
		const val HEADER_Y = 0
	}
}