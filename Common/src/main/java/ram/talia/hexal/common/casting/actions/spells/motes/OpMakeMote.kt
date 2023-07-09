package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.*
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.api.asActionResult
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.getItemEntityOrItemFrame
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.spell.mishaps.MishapStorageFull

/**
 * Mediafy an ItemEntity. This is an [Action] rather than a [ConstMediaAction] or a [SpellAction] so that it can both
 * return things to the stack and have casting particles and a casting sound.
 */
object OpMakeMote : Action {
    fun argc(stack: List<Iota>): Int {
        if (stack.isEmpty())
            return 1
        val top = stack[0]
        if (top is EntityIota)
            return 1
        return 2
    }

    private val mediaCost: Int
        get() = HexalConfig.server.makeItemCost

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()

        val argc = this.argc(stack.reversed())
        if (argc > stack.size)
            throw MishapNotEnoughArgs(argc, stack.size)
        val args = stack.takeLast(argc)
        repeat(argc) { stack.removeLast() }

        val iEntityEither = args.getItemEntityOrItemFrame(0, argc)
        val iEntity = iEntityEither.map({ it }, { it })

        env.assertEntityInRange(iEntity)

        val itemStack = iEntityEither.map( { it.item }, { it.item })
        val mote = if (argc == 2) args.getMote(1, argc) else null

        if (!itemStack.isEmpty) {
            if (mote != null) {
                if (!mote.typeMatches(itemStack))
                    throw MishapInvalidIota.of(mote, 0, "cant_combine_motes")
                val countRemaining = mote.absorb(itemStack)
                if (countRemaining == 0)
                    iEntityEither.map( { it.discard() }, { it.item = ItemStack.EMPTY } )
                else
                    iEntityEither.map( { it.item.count = countRemaining }, { it.item.count = countRemaining } )
                stack.add(mote)
            } else {
                val storage = (env as IMixinCastingContext).boundStorage ?: throw MishapNoBoundStorage(iEntity.position())
                if (!MediafiedItemManager.isStorageLoaded(storage))
                    throw MishapNoBoundStorage(iEntity.position(), "storage_unloaded")
                if (MediafiedItemManager.isStorageFull(storage) != false) // if this is somehow null we should still throw an error here, things have gone pretty wrong
                    throw MishapStorageFull(iEntity.position())

                val itemIota = itemStack.asActionResult(storage)[0]

                if (itemIota !is NullIota)
                    iEntityEither.map( { it.discard() }, { it.item = ItemStack.EMPTY } )

                stack.add(itemIota)
            }
        }

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
        override fun cast(env: CastingEnvironment) { }
    }
}