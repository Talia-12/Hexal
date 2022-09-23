package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.LinkableEntity

object OpLinkEntities : SpellOperator {
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		//TODO: make possible to accept players
		val thisLinkable = args.getChecked<LinkableEntity>(0, argc)
		val other = args.getChecked<LinkableEntity>(1, argc)

		HexalAPI.LOGGER.info("attempting to link $thisLinkable to $other")

		ctx.assertEntityInRange(thisLinkable)
		ctx.assertEntityInRange(other)

		return Triple(
			Spell(thisLinkable, other),
			OpLinkEntity.LINK_COST,
			listOf(ParticleSpray.burst(other.position(), 1.5))
		)
	}

	private data class Spell(val thisLinkable: LinkableEntity, val other: LinkableEntity) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			HexalAPI.LOGGER.info("calling link on $thisLinkable to $other")
			thisLinkable.link(other)
		}
	}
}