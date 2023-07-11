package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.item.ItemEntity
import ram.talia.hexal.api.getMote
import ram.talia.hexal.api.getMoteOrItemStackOrItemEntity

object OpMotesCombinable : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val typer = args.getMote(0, argc) ?: return false.asActionResult
        val typee = args.getMoteOrItemStackOrItemEntity(1, argc) ?: return false.asActionResult

        return typee.flatMap({
            (typer.itemIndex != it.itemIndex && typer.typeMatches(it))
        }, {
            typer.typeMatches(it)
        }, {
            if (it is ItemEntity)
                typer.typeMatches(it.item)
            else
                typer.typeMatches((it as ItemFrame).item)
        }).asActionResult
    }
}