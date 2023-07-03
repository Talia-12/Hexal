package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getPositiveIntUnder
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.getBaseCastingWisp
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

class OpTransferAllowedOthers(val setAllowed: Boolean) : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val wispThis = args.getBaseCastingWisp(0, argc)

        ctx.assertEntityInRange(wispThis)

        if (wispThis.caster == null || wispThis.caster != ctx.caster)
            throw MishapOthersWisp(wispThis.caster)

        val otherIndex = args.getPositiveIntUnder(1, argc, wispThis.numLinked())
        val other = wispThis.getLinked(otherIndex) ?: return null

        return Triple(
            Spell(wispThis, other, setAllowed),
            0,
            listOf()
        )
    }

    private data class Spell(val wispThis: BaseCastingWisp, val other: ILinkable, val setAllowed: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
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