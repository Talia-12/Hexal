package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.Explosion
import ram.talia.hexal.api.linkable.ILinkable

class MishapLinkToSelf(val linkable: ILinkable<*>) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer =
            dyeColor(DyeColor.YELLOW)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component =
            error("self_link", linkable.toString())

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        val pos = linkable.getPosition()
        ctx.world.explode(null, pos.x, pos.y, pos.z, 0.25f, Explosion.BlockInteraction.NONE)
    }
}