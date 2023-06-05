package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.Vec3Iota
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.spell.VarargSpellAction
import ram.talia.hexal.api.spell.iota.MoteIota
import kotlin.math.min

/**
 * Returns Items from being Mediafied back into the world.
 */
object OpReturnMote : VarargSpellAction {
    override fun argc(stack: List<Iota>): Int {
        if (stack.isEmpty())
            return 2
        val top = stack[0]
        if (top is Vec3Iota)
            return 2
        return 3
    }

    override fun execute(args: List<Iota>, argc: Int, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val item = args.getMote(0, argc) ?: return null
        val pos = args.getVec3(1, argc)

        val numToReturn = if (argc == 3) args.getInt(2, argc) else null

        ctx.assertVecInRange(pos)

        return Triple(
                Spell(item, pos, numToReturn),
                HexalConfig.server.returnItemCost,
                listOf(ParticleSpray.burst(pos, 0.5))
        )
    }

    private data class Spell(val item: MoteIota, val pos: Vec3, val numToReturn: Int?) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val toDrop = item.getStacksToDrop(numToReturn?.let { min(it, HexalConfig.server.maxItemsReturned) } ?: HexalConfig.server.maxItemsReturned)

            for (stack in toDrop) {
                ctx.world.addFreshEntity(ItemEntity(ctx.world, pos.x, pos.y, pos.z, stack))
            }
        }
    }
}