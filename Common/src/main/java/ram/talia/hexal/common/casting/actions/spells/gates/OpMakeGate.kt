package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.getVec3
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.gates.GateManager
import ram.talia.hexal.api.spell.VarargConstMediaAction

object OpMakeGate : VarargConstMediaAction {
    override fun argc(stack: List<Iota>): Int {
        if (stack.isEmpty())
            return 1

        return if (stack[0] is EntityIota) 2 else 1
    }

    override val mediaCost: Int
        get() = HexalConfig.server.makeGateCost

    override fun execute(args: List<Iota>, argc: Int, ctx: CastingContext): List<Iota> {
        // if OpMakeGate receives a null, then it'll make a drifting gate that costs proportional to distance but can teleport anywhere in ambit.
        // if it receives a vec and no entity, all teleports will go to the position pointed to by that vec.
        // if it receives a vec and an entity, all teleports will go to that entity, offset by that vec.
        if (args[0] is NullIota)
            return listOf(GateManager.makeGate())

        val vec = args.getVec3(0, argc)

        if (argc == 2) {
            val entity = args.getEntity(1, argc)

            if (entity is ServerPlayer && entity != ctx.caster)
                throw MishapOthersName(entity)

            return listOf(GateManager.makeGate(entity to vec))
        }

        return listOf(GateManager.makeGate(vec))
    }
}