package com.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked

object OpCompareTypes : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val arg0 = args.getChecked<Any>(0, argc)
        val arg1 = args.getChecked<Any>(1, argc)

        // simple, but results in e.g. Horse and Cow being 0 since they're different classes
        // could change to pool all animals together, all monsters together, etc.
        return (arg0::class == arg1::class).asSpellResult
    }
}