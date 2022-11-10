package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.entity.LivingEntity

object OpGetHealth : ConstManaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		return args.getLivingEntityButNotArmorStand(0, argc).health.asActionResult
	}
}