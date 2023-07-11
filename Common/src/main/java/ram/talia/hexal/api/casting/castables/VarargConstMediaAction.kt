package ram.talia.hexal.api.casting.castables

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag

/**
 * An action that always costs the same amount of media, but can accept a variable number of arguments depending on what is on top of the stack.
 */
interface VarargConstMediaAction : Action {
    val mediaCost: Long
        get() = 0

    /**
     * The number of arguments that should be accepted from the stack, given the current state of the stack.
     * If there are not enough args for it to be possible, return the smallest number that could be acceptable.
     * [stack] is the reversed stack, so at index 0 is what's on top of the stack, at index 1 is second from the top,
     * etc.
     */
    fun argc(stack: List<Iota>): Int

    fun execute(args: List<Iota>, argc: Int, userData: CompoundTag, env: CastingEnvironment): List<Iota>

    fun executeWithOpCount(args: List<Iota>, argc: Int, userData: CompoundTag, env: CastingEnvironment): ConstMediaAction.CostMediaActionResult {
        val stack = this.execute(args, argc, userData, env)
        return ConstMediaAction.CostMediaActionResult(stack)
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()

        val argc = this.argc(stack.asReversed())
        if (argc > stack.size)
            throw MishapNotEnoughArgs(argc, stack.size)
        val args = stack.takeLast(argc)
        repeat(argc) { stack.removeLast() }
        val userData = image.userData.copy()
        val newData = this.executeWithOpCount(args, argc, userData, env)
        stack.addAll(newData.resultStack)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMedia(this.mediaCost))

        val image2 = image.copy(stack = stack, opsConsumed = image.opsConsumed + newData.opCount, userData = userData)
        return OperationResult(image2, sideEffects, continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}