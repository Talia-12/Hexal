package ram.talia.hexal.interop.patchouli

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.KEY_DATA
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.getTypeFromTag
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.datafixers.util.Pair
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
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

		val iotaText = getDisplayWithMaxWidth(iota, GuiBook.PAGE_WIDTH, font).iterator()

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

	// Stolen old DisplayWithMaxWidth code from HexIotaTypes and ListIota since they fit better in the Patchouli book.
	private fun getDisplayWithMaxWidth(tag: CompoundTag, maxWidth: Int, font: Font): List<FormattedCharSequence> {
		val type = getTypeFromTag(tag)
				?: return font.split(brokenIota(), maxWidth)
		val data = tag[KEY_DATA]
				?: return font.split(brokenIota(), maxWidth)
		if (type != HexIotaTypes.LIST)
			return font.split(type.display(data), maxWidth)
		return getListDisplayWithMaxWidth(data, maxWidth, font)
	}

	private fun getListDisplayWithMaxWidth(tag: Tag, maxWidth: Int, font: Font): List<FormattedCharSequence> {
		// We aim to not break one iota between lines
		val listTag = tag.downcast(ListTag.TYPE)

		val start = FormattedCharSequence.forward(if (listTag.isEmpty()) "[]" else "[",
				Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE))
		var cursor = font.width(start)
		var currentLine = ArrayList(java.util.List.of(start))
		val out = ArrayList<FormattedCharSequence>()

		for (i in 0 until listTag.size) {
			val subtag = listTag[i]
			val cSubtag = subtag.downcast(CompoundTag.TYPE)
			val translation = HexIotaTypes.getDisplay(cSubtag)
			var currentElement = translation.visualOrderText
			val addl = if (i < listTag.size - 1) {
				", "
			} else {
				// Last go-around, so add the closing bracket
				"]"
			}
			currentElement = FormattedCharSequence.composite(currentElement,
					FormattedCharSequence.forward(addl, Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)))
			val width = font.width(currentElement)
			if (cursor + width > maxWidth) {
				out.add(FormattedCharSequence.composite(currentLine))
				currentLine = ArrayList()
				// Indent further lines by two spaces
				val indentation = FormattedCharSequence.forward("  ", Style.EMPTY)
				val lineStart = FormattedCharSequence.composite(indentation, currentElement)
				currentLine.add(lineStart)
				cursor = font.width(lineStart)
			} else {
				currentLine.add(currentElement)
				cursor += width
			}
		}

		if (currentLine.isNotEmpty()) {
			out.add(FormattedCharSequence.composite(currentLine))
		}

		return out
	}

	private fun brokenIota(): Component {
		return Component.translatable("hexcasting.spelldata.unknown")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
	}

	companion object {
		const val HEADER_X = GuiBook.PAGE_WIDTH / 2
		const val HEADER_Y = 0
		const val DATA_X = 0
		const val DATA_Y = 100
	}
}