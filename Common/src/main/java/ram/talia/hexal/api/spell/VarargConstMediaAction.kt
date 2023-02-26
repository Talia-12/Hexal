package ram.talia.hexal.api.spell

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.casting.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

/**
 * An action that always costs the same amount of media, but can accept a variable number of arguments depending on what is on top of the stack.
 */
interface VarargConstMediaAction : Action {
    val mediaCost: Int
        get() = 0

    fun argc(stack: List<Iota>): Int

    fun execute(args: List<Iota>, ctx: CastingContext): List<Iota>

    override fun operate(
            continuation: SpellContinuation,
            stack: MutableList<Iota>,
            ravenmind: Iota?,
            ctx: CastingContext
    ): OperationResult {
        val argc = this.argc(stack.asReversed())
        if (argc > stack.size)
            throw MishapNotEnoughArgs(argc, stack.size)
        val args = stack.takeLast(argc)
        repeat(argc) { stack.removeLast() }
        val newData = this.execute(args, ctx)
        stack.addAll(newData)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMedia(this.mediaCost))

        return OperationResult(continuation, stack, ravenmind, sideEffects)
    }
}