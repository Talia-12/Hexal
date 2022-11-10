package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.IMixinCastingContext
import ram.talia.hexal.api.spell.mishaps.MishapLinkToSelf
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object OpLinkEntity : SpellAction {
	const val LINK_COST = MediaConstants.SHARD_UNIT

	override val argc = 1

	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val mCast = ctx as? IMixinCastingContext

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		val other = args.getEntity(0, argc)
		if (other !is LinkableEntity && other !is ServerPlayer)
			throw MishapInvalidIota.ofType(EntityIota(other), 0, "entity.linkable")

		val linkOther = when (other) {
			is LinkableEntity -> other
			is ServerPlayer -> IXplatAbstractions.INSTANCE.getLinkstore(other)
			else -> throw Exception("How did I get here")
		}

		if (linkThis == linkOther)
			throw MishapLinkToSelf(linkThis)

		if (!linkThis.isInRange(linkOther))
			throw MishapLocationTooFarAway(linkOther.getPos())

		return Triple(
			Spell(linkThis, linkOther),
			LINK_COST,
			listOf(ParticleSpray.burst(other.position(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable<*>, val linkOther: ILinkable<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.link(linkOther)
	}
}