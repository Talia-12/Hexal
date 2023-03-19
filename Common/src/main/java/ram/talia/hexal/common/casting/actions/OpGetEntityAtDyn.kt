package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntitiesBy
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.getEntityType

object OpGetEntityAtDyn : ConstMediaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val type = args.getEntityType(0, argc)
        val pos = args.getVec3(1, argc)
        ctx.assertVecInRange(pos)
        val aabb = AABB(pos.add(Vec3(-0.5, -0.5, -0.5)), pos.add(Vec3(0.5, 0.5, 0.5)))
        val entitiesGot = ctx.world.getEntities(null, aabb) {
            OpGetEntitiesBy.isReasonablySelectable(ctx, it) && it.type == type
        }.sortedBy { it.distanceToSqr(pos) }

        val entity = entitiesGot.getOrNull(0)
        return entity.asActionResult
    }
}