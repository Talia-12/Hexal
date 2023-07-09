package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Level
import ram.talia.hexal.api.linkable.ILinkable

class MishapLinkToSelf(val linkable: ILinkable) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.YELLOW)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = error("self_link", linkable.toString())

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val pos = linkable.getPosition()
        env.world.explode(null, pos.x, pos.y, pos.z, 0.25f, Level.ExplosionInteraction.NONE)
    }
}