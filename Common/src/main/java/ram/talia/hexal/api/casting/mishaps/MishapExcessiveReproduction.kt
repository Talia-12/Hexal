package ram.talia.hexal.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import ram.talia.hexal.common.entities.BaseCastingWisp

class MishapExcessiveReproduction(val wisp: BaseCastingWisp) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.PINK)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = error("excessive_reproduction", wisp)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.world.sendParticles(ParticleTypes.HEART, wisp.position().x, wisp.position().y, wisp.position().z, 15, 0.5, 0.5, 0.5, 0.5)
    }
}