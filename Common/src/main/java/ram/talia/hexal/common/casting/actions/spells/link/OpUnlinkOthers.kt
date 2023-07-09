package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.spell.mishaps.MishapLinkToSelf

object OpUnlinkOthers : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val linkThis = LinkableRegistry.linkableFromIota(args[0], env.world)
                ?: throw MishapInvalidIota.ofType(args[0], 1, "linkable")
        val linkOther = LinkableRegistry.linkableFromIota(args[1], env.world)
                ?: throw MishapInvalidIota.ofType(args[1], 0, "linkable")

        if (linkThis == linkOther)
            throw MishapLinkToSelf(linkThis)

        env.assertVecInRange(linkThis.getPosition())
        env.assertVecInRange(linkOther.getPosition())

        return SpellAction.Result(
                Spell(linkThis, linkOther),
                HexalConfig.server.unlinkCost,
                listOf(ParticleSpray.burst(linkThis.getPosition(), 1.5), ParticleSpray.burst(linkOther.getPosition(), 1.5))
        )
    }

    private data class Spell(val linkThis: ILinkable, val other: ILinkable) : RenderedSpell {
        override fun cast(env: CastingEnvironment) = if (linkThis.getLinkedIndex(other) != -1) linkThis.unlink(other) else Unit
    }
}