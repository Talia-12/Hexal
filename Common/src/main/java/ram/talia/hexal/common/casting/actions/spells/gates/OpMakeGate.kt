package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.gates.GateManager

object OpMakeGate : ConstMediaAction {
    override val argc = 0

    override val mediaCost: Int
        get() = HexalConfig.server.makeGateCost

    override fun execute(args: List<Iota>, ctx: CastingContext) = listOf(GateManager.makeGate())
}