package ram.talia.hexal.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntitiesBy
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.plus

class MishapNoBoundStorage(val reason: String? = null) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.LIME)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = if (reason != null) error(reason) else error("no_bound_storage")

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val pos = env.mishapSprayPos()
        val radius = 5.0
        val aabb = AABB(pos.add(Vec3(-radius, -radius, -radius)), pos.add(Vec3(radius, radius, radius)))
        val nearbyItems = env.world.getEntities(null, aabb) {
            OpGetEntitiesBy.isReasonablySelectable(env, it)
                && it.distanceToSqr(pos) <= radius * radius
                && it is ItemEntity
        }

        for (item in nearbyItems) {
            item.deltaMovement += Vec3(env.world.random.nextDouble(), env.world.random.nextDouble(), env.world.random.nextDouble())
        }
    }
}
