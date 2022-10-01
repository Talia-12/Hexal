package ram.talia.hexal.interop.patchouli

import vazkii.patchouli.api.IComponentProcessor
import vazkii.patchouli.api.IVariable
import vazkii.patchouli.api.IVariableProvider

class EverbookPatternProcessor : IComponentProcessor {
	override fun setup(variables: IVariableProvider) { }

	override fun process(key: String): IVariable? {
		return null
	}
}