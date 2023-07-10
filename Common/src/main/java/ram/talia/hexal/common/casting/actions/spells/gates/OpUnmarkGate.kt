package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.getGate
import ram.talia.hexal.api.casting.iota.GateIota

object OpUnmarkGate : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val gate = args.getGate(0, OpMarkGate.argc)
        val entity = args.getEntity(1, OpMarkGate.argc)
        env.assertEntityInRange(entity)

        return SpellAction.Result(
            Spell(gate, entity),
            0,
                listOf(ParticleSpray.cloud(entity.position(), 1.0))
        )
    }

    private class Spell(val gate: GateIota, val entity: Entity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            gate.unmark(entity)
        }
    }
}