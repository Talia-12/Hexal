package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getItem
import ram.talia.hexal.api.spell.iota.ItemIota

/**
 * Returns Items from being Mediafied back into the world.
 */
object OpReturnItem : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val item = args.getItem(0, argc) ?: return null
        val numToReturn = args.getInt(1, argc)
        val pos = args.getVec3(2, argc)

        ctx.assertVecInRange(pos)

        return Triple(
                Spell(item, numToReturn, pos),
                HexalConfig.server.returnItemCost,
                listOf(ParticleSpray.burst(pos, 0.5))
        )
    }

    private data class Spell(val item: ItemIota, val numToReturn: Int, val pos: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val toDrop = item.getStacksToDrop(numToReturn)

            for (stack in toDrop) {
                ctx.world.addFreshEntity(ItemEntity(ctx.world, pos.x, pos.y, pos.z, stack))
            }
        }
    }
}