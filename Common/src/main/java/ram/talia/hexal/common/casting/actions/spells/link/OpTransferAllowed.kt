package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.casting.mishaps.MishapNoWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

class OpTransferAllowed(private val setAllowed: Boolean) : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val wispThis = (env as? WispCastEnv)?.wisp ?: throw MishapNoWisp()

        val otherIndex = args.getPositiveIntUnder(0, OpSendIota.argc, wispThis.numLinked())
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