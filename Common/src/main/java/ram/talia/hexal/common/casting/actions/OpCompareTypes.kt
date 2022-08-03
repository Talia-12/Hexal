package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked

object OpCompareTypes : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        // simple, but results in e.g. Horse and Cow being 0 since they're different classes
        // could change to pool all animals together, all monsters together, etc.
        return (args[0].getType() == args[1].getType()).asSpellResult
    }
}