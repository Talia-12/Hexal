package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.common.blocks.BlockMediafiedStorage
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions

class OpBindStorage(val isTemporaryBinding: Boolean) : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getBlockPos(0, argc)

        ctx.assertVecInRange(pos)

        val storage = ctx.world.getBlockState(pos).block

        return Triple(
            Spell(if (storage is BlockMediafiedStorage) pos else null, isTemporaryBinding),
            if (isTemporaryBinding) HexalConfig.server.bindTemporaryStorageCost else HexalConfig.server.bindStorageCost,
            listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 1.5))
        )
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private data class Spell(val pos: BlockPos?, val isTemporaryBinding: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (pos == null) {
                MediafiedItemManager.setBoundStorage(ctx.caster, null)
                return
            }

            if (!ctx.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isInteractingAllowed(ctx.world, pos, Direction.UP, ctx.castingHand, ctx.caster))
                return

            val storage = ctx.world.getBlockEntity(pos) as? BlockEntityMediafiedStorage ?: return

            if (isTemporaryBinding)
                (ctx as IMixinCastingContext).setTemporaryBoundStorage(storage.uuid)
            else
                MediafiedItemManager.setBoundStorage(ctx.caster, storage.uuid)
        }
    }
}