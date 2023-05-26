package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import ram.talia.hexal.common.entities.BaseCastingWisp

class MishapExcessiveReproduction(val wisp: BaseCastingWisp) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.PINK)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("excessive_reproduction", wisp)

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        ctx.world.sendParticles(ParticleTypes.HEART, wisp.position().x, wisp.position().y, wisp.position().z, 15, 0.5, 0.5, 0.5, 0.5)
    }
}