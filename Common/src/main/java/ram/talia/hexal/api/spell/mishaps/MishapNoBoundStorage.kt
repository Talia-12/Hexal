package ram.talia.hexal.api.spell.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntitiesBy
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.plus

class MishapNoBoundStorage(val pos: Vec3) : Mishap() {
    override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.LIME)

    override fun errorMessage(ctx: CastingContext, errorCtx: Context): Component = error("no_bound_storage")

    override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {
        val radius = 5.0
        val aabb = AABB(pos.add(Vec3(-radius, -radius, -radius)), pos.add(Vec3(radius, radius, radius)))
        val nearbyItems = ctx.world.getEntities(null, aabb) {
            OpGetEntitiesBy.isReasonablySelectable(ctx, it)
                && it.distanceToSqr(pos) <= radius * radius
                && it is ItemEntity
        }

        for (item in nearbyItems) {
            item.deltaMovement += Vec3(ctx.world.random.nextDouble(), ctx.world.random.nextDouble(), ctx.world.random.nextDouble())
        }
    }
}
