package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.minecraft.core.Direction
import ram.talia.hexal.api.getMote
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions

/**
 * Moves the ItemRecord pointed to by an item iota to a different MediafiedStorage. **Invalidates all previous item iotas pointing to that ItemRecord**.
 */
object OpSetMoteStorage : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val item = args.getMote(0, argc) ?: return null.asActionResult
        val storagePos = args.getBlockPos(1, argc)

        env.assertVecInRange(storagePos.center)

        if (!env.canEditBlockAt(storagePos) || !IXplatAbstractions.INSTANCE.isInteractingAllowed(env.world, storagePos, Direction.UP, env.castingHand, env.caster))
            return listOf(item)

        val storage = env.world.getBlockEntity(storagePos) as? BlockEntityMediafiedStorage ?: throw MishapInvalidIota.ofType(args[1], 0, "mediafied_storage")

        val newItem = item.setStorage(storage.uuid)

        return newItem?.let { listOf(it) } ?: null.asActionResult
    }
}