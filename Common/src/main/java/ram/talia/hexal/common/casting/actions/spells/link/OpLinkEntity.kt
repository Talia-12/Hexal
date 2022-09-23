package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object OpLinkEntity : SpellOperator {
	const val LINK_COST = ManaConstants.SHARD_UNIT

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		val other = args.getChecked<Entity>(0, argc)
		if (other !is LinkableEntity && other !is ServerPlayer)
			throw MishapInvalidIota.ofClass(SpellDatum.make(other), 0, LinkableEntity::class.java)

		val linkOther = when (other) {
			is LinkableEntity -> other
			is ServerPlayer -> IXplatAbstractions.INSTANCE.getLinkstore(other)
			else -> throw Exception("How did I get here")
		}

		//TODO: add ILinkable.maxSqrLinkRange
//		if ((linkThis.getPos() - other.position()).lengthSqr() > linkThis.maxSqrCastingDistance())
//			throw MishapEntityTooFarAway(other)

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