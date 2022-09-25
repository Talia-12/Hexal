package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LightLayer
import net.minecraft.world.phys.Vec3

object OpGetLightLevel : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val vec = args.getChecked<Vec3>(0, argc)
		val pos = BlockPos(vec)

		return ctx.world.getBrightness(LightLayer.BLOCK, pos).asSpellResult
	}
}