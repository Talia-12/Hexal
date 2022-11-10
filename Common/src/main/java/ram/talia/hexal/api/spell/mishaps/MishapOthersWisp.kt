package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor

class MishapOthersWisp(val other: Player?) : Mishap() {
	override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.BLACK)

	override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("others_wisp", other?.name ?: "Unowned")

	override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
		ctx.caster.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 60))
	}
}