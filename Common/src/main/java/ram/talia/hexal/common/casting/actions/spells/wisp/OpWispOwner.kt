package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.world.entity.Entity
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.BaseWisp

object OpWispOwner : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val wisp = args.getChecked<BaseWisp>(0, argc)
        val owner = args.getChecked<Entity>(1, argc)

        return (wisp is BaseCastingWisp && wisp.caster == owner).asSpellResult
    }
}