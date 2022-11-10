package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.level.LightLayer

object OpGetLightLevel : ConstManaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val pos = args.getBlockPos(0, argc)

		return ctx.world.getBrightness(LightLayer.BLOCK, pos).asActionResult
	}
}