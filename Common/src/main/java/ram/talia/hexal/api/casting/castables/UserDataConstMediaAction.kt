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
 * A SimpleOperator that always costs the same amount of media. Provides access to userData.
 */
interface UserDataConstMediaAction : Action {
    val argc: Int
    val mediaCost: Int
        get() = 0

    fun execute(args: List<Iota>, userData: CompoundTag, env: CastingEnvironment): List<Iota>

    fun executeWithOpCount(args: List<Iota>, userData: CompoundTag, env: CastingEnvironment): ConstMediaAction.CostMediaActionResult {
        val stack = this.execute(args, userData, env)
        return ConstMediaAction.CostMediaActionResult(stack)
    }

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()

        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        repeat(this.argc) { stack.removeLast() }
        val userData = image.userData.copy()
        val result = executeWithOpCount(args, userData, env)
        stack.addAll(result.resultStack)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMedia(this.mediaCost))

        val image2 = image.copy(stack = stack, opsConsumed = image.opsConsumed + result.opCount, userData = userData)
        return OperationResult(image2, sideEffects, continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}