package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.DyeColor

class MishapNonPlayer : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.LIGHT_BLUE)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("non_player", actionName(errorCtx.action))

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<SpellDatum<*>>) {
        ctx.caster.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 60))
    }
}