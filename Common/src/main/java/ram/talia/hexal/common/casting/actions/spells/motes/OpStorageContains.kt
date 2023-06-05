package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.core.Direction
import ram.talia.hexal.api.getMoteOrItemType
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions

object OpStorageContains : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val storagePos = args.getBlockPos(0, argc)
        val moteOrItem = args.getMoteOrItemType(1, argc)

        ctx.assertVecInRange(storagePos)

        if (!ctx.canEditBlockAt(storagePos) || !IXplatAbstractions.INSTANCE.isInteractingAllowed(ctx.world, storagePos, Direction.UP, ctx.castingHand, ctx.caster))
            return false.asActionResult

        val storage = ctx.world.getBlockEntity(storagePos) as? BlockEntityMediafiedStorage ?: throw MishapInvalidIota.ofType(args[1], 0, "mediafied_storage")

        return moteOrItem?.map(
                { itemIota -> itemIota.record?.let { storage.getItemRecordsMatching(it) }?.isNotEmpty() ?: false },
                { item -> storage.getItemRecordsMatching(item).isNotEmpty() }
        )?.asActionResult ?: false.asActionResult
    }
}