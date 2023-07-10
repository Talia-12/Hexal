package ram.talia.hexal.interop.patchouli

import net.minecraft.world.level.Level
import vazkii.patchouli.api.IComponentProcessor
import vazkii.patchouli.api.IVariable
import vazkii.patchouli.api.IVariableProvider

class EverbookPatternProcessor : IComponentProcessor {
	override fun setup(level: Level, variables: IVariableProvider) { }

	override fun process(level: Level, key: String): IVariable = IVariable.empty()
}