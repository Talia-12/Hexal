package ram.talia.hexal.interop.patchouli

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component
import vazkii.patchouli.api.IComponentRenderContext
import vazkii.patchouli.api.ICustomComponent
import vazkii.patchouli.api.IVariable
import vazkii.patchouli.client.book.gui.GuiBook
import java.util.function.UnaryOperator

class EverbookHeaderComponent : ICustomComponent {
	@Transient
	var x = -1
	@Transient
	var y = -1
	@Transient
	private var pageNum = 0

	var centered = true
	var scale = 1f

	@Transient
	var actualText: Component? = null

	override fun onVariablesAvailable(lookup: UnaryOperator<IVariable>) {

	}

	override fun build(x: Int, y: Int, pageNum: Int) {
		this.x = x
		this.y = y
		this.pageNum = pageNum

		if (this.x == -1)
			this.x = GuiBook.PAGE_WIDTH / 2
		if (this.y == -1)
			this.y = 0
	}

	override fun render(ms: PoseStack, context: IComponentRenderContext, pticks: Float, mouseX: Int, mouseY: Int) {
		ms.pushPose()
		ms.translate(x.toDouble(), y.toDouble(), 0.0)
		ms.scale(scale, scale, scale)

		if (centered) {

//			page.parent.drawCenteredStringNoShadow(ms, page.i18n(actualText!!.string), 0, 0, color)
		} else {
//			page.fontRenderer.draw(ms, page.i18n(actualText!!.string), 0f, 0f, color)
		}
		ms.popPose()
	}
}