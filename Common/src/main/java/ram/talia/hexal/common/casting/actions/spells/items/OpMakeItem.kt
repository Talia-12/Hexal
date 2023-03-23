package ram.talia.hexal.common.casting.actions.spells.items

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.casting.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

/**
 * Mediafy an ItemEntity. This is an [Action] rather than a [ConstMediaAction] or a [SpellAction] so that it can both
 * return things to the stack and have casting particles and a casting sound.
 */
object OpMakeItem : Action {
    const val argc = 1
    private val mediaCost: Int
        get() = HexalConfig.server.makeItemCost

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun operate(continuation: SpellContinuation, stack: MutableList<Iota>, ravenmind: Iota?, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        repeat(this.argc) { stack.removeLast() }

        val iEntity = args.getItemEntity(0, argc)

        ctx.assertEntityInRange(iEntity)

        val itemStack = iEntity.item
        val storage = (ctx as IMixinCastingContext).boundStorage ?: throw MishapNoBoundStorage(iEntity.position())
        if (!MediafiedItemManager.isStorageLoaded(storage))
            throw MishapNoBoundStorage(iEntity.position(), "storage_unloaded")
        if (MediafiedItemManager.isStorageFull(storage) != false) // if this is somehow null we should still throw an error here, things have gone pretty wrong
            throw MishapStorageFull(iEntity.position())

        val itemIota = itemStack.asActionResult(storage)[0]

        if (itemIota !is NullIota)
            iEntity.discard()

        stack.add(itemIota)

        val sideEffects = mutableListOf(
                OperatorSideEffect.ConsumeMedia(this.mediaCost),
                OperatorSideEffect.AttemptSpell(
                        Spell,
                        hasCastingSound = true,
                        awardStat = true
                ),
                OperatorSideEffect.Particles(ParticleSpray.burst(iEntity.position(), 0.4))
        )

        return OperationResult(continuation, stack, ravenmind, sideEffects)
    }

    /**
     * Exists solely to get casting sounds.
     */
    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) { }
    }
}