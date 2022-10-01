package ram.talia.hexal.interop.patchouli

import at.petrak.hexcasting.api.spell.math.HexCoord
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.interop.patchouli.AbstractPatternComponent
import com.mojang.datafixers.util.Pair
import ram.talia.hexal.xplat.IClientXplatAbstractions
import vazkii.patchouli.api.IComponentRenderContext
import vazkii.patchouli.api.IVariable
import java.util.function.UnaryOperator

class EverbookPatternComponent : AbstractPatternComponent() {
	var pageNum: Int = -1

	override fun build(x: Int, y: Int, pagenum: Int) {
		super.build(x, y, pagenum)
		pageNum = pagenum
	}

	override fun getPatterns(lookup: UnaryOperator<IVariable>): List<Pair<HexPattern, HexCoord>> {
		val pattern: HexPattern? = IClientXplatAbstractions.INSTANCE.getClientEverbookPattern(pageNum)

		return if (pattern != null) listOf(Pair(pattern, HexCoord.Origin)) else listOf()
	}

	override fun onDisplayed(context: IComponentRenderContext) {
		onVariablesAvailable { it }
	}

	override fun showStrokeOrder() = true
}