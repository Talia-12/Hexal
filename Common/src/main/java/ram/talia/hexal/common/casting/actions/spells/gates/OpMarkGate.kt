package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.common.lib.HexEntityTags
import ram.talia.hexal.api.getGate

object OpMarkGate : ConstMediaAction {
    override val argc = 2

    override val mediaCost = MediaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val gate = args.getGate(0, argc)
        val entity = args.getEntity(0, argc)
        ctx.assertEntityInRange(entity)

        if (!entity.canChangeDimensions() || entity.type.`is`(HexEntityTags.CANNOT_TELEPORT))
            throw MishapImmuneEntity(entity)

        gate.mark(entity)

        return listOf()
    }
}