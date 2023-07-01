package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

class OpTransferAllowed(val setAllowed: Boolean) : SpellAction {
    override val argc = 1

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val mCast = ctx as? IMixinCastingContext
        val wispThis = mCast?.wisp ?: throw MishapNoWisp()

        val otherIndex = args.getPositiveIntUnder(0, OpSendIota.argc, wispThis.numLinked())
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