package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPositiveIntUnder
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.api.getBaseCastingWisp
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.casting.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

class OpTransferAllowedOthers(private val setAllowed: Boolean) : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val wispThis = args.getBaseCastingWisp(0, argc)

        env.assertEntityInRange(wispThis)

        if (wispThis.caster == null || wispThis.caster != env.caster)
            throw MishapOthersWisp(wispThis.caster)

        val otherIndex = args.getPositiveIntUnder(1, argc, wispThis.numLinked())
        val other = wispThis.getLinked(otherIndex)
            ?: throw MishapInvalidIota.of(args[0], 1, "linkable.index")

        return SpellAction.Result(
            Spell(wispThis, other, setAllowed),
            0,
            listOf()
        )
    }

    private data class Spell(val wispThis: BaseCastingWisp, val other: ILinkable, val setAllowed: Boolean) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (setAllowed) {
                wispThis.removeFromBlackListTransferMedia(other)
                wispThis.addToWhiteListTransferMedia(other)
            } else {
                wispThis.addToBlackListTransferMedia(other)
                wispThis.removeFromWhiteListTransferMedia(other)
            }
        }
    }
}