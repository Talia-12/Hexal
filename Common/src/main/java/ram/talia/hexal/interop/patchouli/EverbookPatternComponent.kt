package ram.talia.hexal.interop.patchouli

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.datafixers.util.Pair
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
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

		if (iota != null) {
			val iotaComponent = displayFromNBT(iota!!)
			drawWrappedString(poseStack, iotaComponent, DATA_X, DATA_Y, 0)
		}

		super.render(poseStack, ctx, partialTicks, mouseX, mouseY)
	}

	override fun showStrokeOrder() = true

	private fun drawCenteredStringNoShadow(ms: PoseStack, s: Component, x: Int, y: Int, colour: Int) {
		val font = Minecraft.getInstance().font
		font.draw(ms, s, x - font.width(s) / 2.0f, y.toFloat(), colour)
	}

	private fun drawWrappedString(ms: PoseStack, s: Component, x: Int, y: Int, colour: Int) {
		val font = Minecraft.getInstance().font
		val iter = font.split(s, GuiBook.PAGE_WIDTH).iterator()

		var iterY = y

		while (iter.hasNext() && iterY <= y + 5 * 9) { // don't draw more lines than fit in the book.
			ms.pushPose()
			ms.translate(x.toDouble(), iterY.toDouble(), 0.0)
			val toDraw = if (iterY < y + 5 * 9) { iter.next() } else { "...".red.visualOrderText }
			font.draw(ms, toDraw, 0.0f, 0.0f, colour)
			ms.popPose()
			iterY += 9
		}
//		for(Iterator var7 = this.split($$0, $$3).iterator(); var7.hasNext(); $$2 += 9) {
//			FormattedCharSequence $$6 = (FormattedCharSequence)var7.next();
//			this.drawInternal($$6, (float)$$1, (float)$$2, $$4, $$5, false);
//		}
//		font.drawWordWrap(s, x, y, GuiBook.PAGE_WIDTH, 0)
	}

	private fun displayFromNBT(nbt: CompoundTag): Component {
		val keys = nbt.allKeys
		val out = "".asTextComponent

		if (keys.size != 1)
			out += "hexcasting.spelldata.unknown".asTranslatedComponent.black
		else {
			when (val key = keys.iterator().next()) {
				SpellDatum.TAG_DOUBLE -> out += String.format(
					"%.4f",
					nbt.getDouble(SpellDatum.TAG_DOUBLE)
				).green
				SpellDatum.TAG_VEC3 -> {
					val vec = vecFromNBT(nbt.getLongArray(key))
					// the focus color is really more red, but we don't want to show an error-y color
					out += String.format(
						"(%.2f, %.2f, %.2f)",
						vec.x,
						vec.y,
						vec.z
					).lightPurple
				}
				SpellDatum.TAG_LIST -> {
					out += "[".black

					val arr = nbt.getList(key, Tag.TAG_COMPOUND)
					for ((i, subtag) in arr.withIndex()) {
						out += displayFromNBT(subtag.asCompound)
						if (i != arr.lastIndex) {
							out += ", ".black
						}
					}

					out += "]".black
				}
				SpellDatum.TAG_WIDGET -> {
					val widget = Widget.fromString(nbt.getString(key))

					out += if (widget == Widget.GARBAGE)
						"arimfexendrapuse".darkGray.obfuscated
					else
						widget.toString().darkPurple
				}
				SpellDatum.TAG_PATTERN -> {
					val pat = HexPattern.fromNBT(nbt.getCompound(SpellDatum.TAG_PATTERN))
					var angleDesc = pat.anglesSignature()
					if (angleDesc.isNotBlank()) angleDesc = " $angleDesc"
					out += "HexPattern(".gold
					out += "${pat.startDir}$angleDesc".black
					out += ")".gold
				}
				SpellDatum.TAG_ENTITY -> {
					val subtag = nbt.getCompound(SpellDatum.TAG_ENTITY)
					val json = subtag.getString(SpellDatum.TAG_ENTITY_NAME_CHEATY)
					// handle pre-0.5.0 foci not having the tag
					out += Component.Serializer.fromJson(json)?.aqua
						?: "hexcasting.spelldata.entity.whoknows".asTranslatedComponent.black
				}
				else -> {
					out += "hexcasting.spelldata.unknown".asTranslatedComponent.black
				}
			}
		}
		return out
	}

	companion object {
		const val HEADER_X = GuiBook.PAGE_WIDTH / 2
		const val HEADER_Y = 0
		const val DATA_X = 0
		const val DATA_Y = 100
	}
}