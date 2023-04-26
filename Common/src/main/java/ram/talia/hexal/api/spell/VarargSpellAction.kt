package ram.talia.hexal.api.spell

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.casting.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

/**
 * An action that has some affect on the world, and takes a variable number of arguments depending on what's on the stack.
 */
interface VarargSpellAction : Action {
    /**
     * The number of arguments that should be accepted from the stack, given the current state of the stack.
     * If there are not enough args for it to be possible, return the smallest number that could be acceptable.
     * [stack] is the reversed stack, so at index 0 is what's on top of the stack, at index 1 is second from the top,
     * etc.
     */
    fun argc(stack: List<Iota>): Int

    fun hasCastingSound(ctx: CastingContext): Boolean = true

    fun awardsCastingStat(ctx: CastingContext): Boolean = true

    fun execute(
            args: List<Iota>,
            argc: Int,
            ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>?

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
        for (_i in 0 until argc) stack.removeLast()
        val executeResult = this.execute(args, argc, ctx) ?: return OperationResult(continuation, stack, ravenmind, listOf())
        val (spell, media, particles) = executeResult

        val sideEffects = mutableListOf<OperatorSideEffect>()

        if (media > 0)
            sideEffects.add(OperatorSideEffect.ConsumeMedia(media))

        // Don't have an effect if the caster isn't enlightened, even if processing other side effects
        if (!isGreat || ctx.isCasterEnlightened)
            sideEffects.add(
                OperatorSideEffect.AttemptSpell(
                    spell,
                    this.hasCastingSound(ctx),
                    this.awardsCastingStat(ctx)
                )
            )

        for (spray in particles)
            sideEffects.add(OperatorSideEffect.Particles(spray))

        return OperationResult(continuation, stack, ravenmind, sideEffects)
    }
}