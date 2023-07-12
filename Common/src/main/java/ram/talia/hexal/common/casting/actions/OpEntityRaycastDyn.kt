package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB
import ram.talia.hexal.api.getEntityTypeOrItemType

object OpEntityRaycastDyn : ConstMediaAction {
    override val argc = 2
    override val mediaCost = MediaConstants.DUST_UNIT / 100

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val type = args.getEntityTypeOrItemType(0, argc)
        val origin = args.getVec3(1, argc)
        val look = args.getVec3(2, argc)
        val endp = Action.raycastEnd(origin, look)

        ctx.assertVecInRange(origin)

        val entityHitResult = ProjectileUtil.getEntityHitResult(
            ctx.caster,
            origin,
            endp,
            AABB(origin, endp),
            { entity -> type.map({ entity.type == it }, { entity is ItemEntity && entity.item.item == it }) },
            1_000_000.0
        )

        return if (entityHitResult != null && ctx.isEntityInRange(entityHitResult.entity)) {
            entityHitResult.entity.asActionResult
        } else {
            listOf(NullIota())
        }
    }
}