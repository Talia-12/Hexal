package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object OpLinkEntities : SpellOperator {
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		//TODO: make possible to accept players
		val entityThis = args.getChecked<Entity>(0, OpLinkEntity.argc)
		if (entityThis !is LinkableEntity && entityThis !is ServerPlayer)
			throw MishapInvalidIota.ofClass(SpellDatum.make(entityThis), 1, LinkableEntity::class.java)

		val linkThis = when (entityThis) {
			is LinkableEntity -> entityThis
			is ServerPlayer -> IXplatAbstractions.INSTANCE.getLinkstore(entityThis)
			else -> throw Exception("How did I get here")
		}

		val entityOther = args.getChecked<Entity>(1, OpLinkEntity.argc)
		if (entityOther !is LinkableEntity && entityOther !is ServerPlayer)
			throw MishapInvalidIota.ofClass(SpellDatum.make(entityOther), 0, LinkableEntity::class.java)

		val linkOther = when (entityOther) {
			is LinkableEntity -> entityOther
			is ServerPlayer -> IXplatAbstractions.INSTANCE.getLinkstore(entityOther)
			else -> throw Exception("How did I get here")
		}

		ctx.assertEntityInRange(entityThis)
		ctx.assertEntityInRange(entityOther)

		//TODO: add ILinkable.maxSqrLinkRange
//		if ((linkThis.getPos() - linkOther.getPos()).lengthSqr() > linkThis.maxSqrCastingDistance())
//			throw MishapEntityTooFarAway(other)

		return Triple(
			Spell(linkThis, linkOther),
			OpLinkEntity.LINK_COST,
			listOf(ParticleSpray.burst(entityOther.position(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable<*>, val linkOther: ILinkable<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.link(linkOther)
	}
}