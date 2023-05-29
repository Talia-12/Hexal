package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.linkable.LinkableRegistry
import ram.talia.hexal.api.spell.mishaps.MishapLinkToSelf

object OpUnlinkOthers : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val linkThis = LinkableRegistry.linkableFromIota(args[0], ctx.world)
                ?: throw MishapInvalidIota.ofType(args[0], 1, "linkable")
        val linkOther = LinkableRegistry.linkableFromIota(args[1], ctx.world)
                ?: throw MishapInvalidIota.ofType(args[1], 0, "linkable")

        if (linkThis == linkOther)
            throw MishapLinkToSelf(linkThis)

        ctx.assertVecInRange(linkThis.getPosition())
        ctx.assertVecInRange(linkOther.getPosition())

        return Triple(
                Spell(linkThis, linkOther),
                HexalConfig.server.unlinkCost,
                listOf(ParticleSpray.burst(linkThis.getPosition(), 1.5), ParticleSpray.burst(linkOther.getPosition(), 1.5))
        )
    }

    private data class Spell(val linkThis: ILinkable, val other: ILinkable) : RenderedSpell {
        override fun cast(ctx: CastingContext) = if (linkThis.getLinkedIndex(other) != -1) linkThis.unlink(other) else Unit
    }
}