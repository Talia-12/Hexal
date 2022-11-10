package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapEntityTooFarAway
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.mishaps.MishapLinkToSelf
import ram.talia.hexal.common.entities.LinkableEntity
import ram.talia.hexal.xplat.IXplatAbstractions

object OpLinkEntities : SpellAction {
	override val argc = 2

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val entityThis = args.getEntity(0, OpLinkEntity.argc)

		val linkThis = when (entityThis) {
			is LinkableEntity -> entityThis
			is ServerPlayer -> IXplatAbstractions.INSTANCE.getLinkstore(entityThis)
			else -> throw MishapInvalidIota.ofType(EntityIota(entityThis), 1, "entity.linkable")
		}

		val entityOther = args.getEntity(1, OpLinkEntity.argc)

		if (entityThis.uuid == entityOther.uuid)
			throw MishapLinkToSelf(linkThis)

		val linkOther = when (entityOther) {
			is LinkableEntity -> entityOther
			is ServerPlayer -> IXplatAbstractions.INSTANCE.getLinkstore(entityOther)
			else -> throw MishapInvalidIota.ofType(EntityIota(entityOther), 0, "entity.linkable")
		}

		ctx.assertEntityInRange(entityThis)
		ctx.assertEntityInRange(entityOther)

		if (!linkThis.isInRange(linkOther))
			throw MishapLocationTooFarAway(linkOther.getPos())

		return Triple(
			Spell(linkThis, linkOther),
			OpLinkEntity.LINK_COST,
			listOf(ParticleSpray.burst(linkThis.getPos(), 1.5), ParticleSpray.burst(linkOther.getPos(), 1.5))
		)
	}

	private data class Spell(val linkThis: ILinkable<*>, val linkOther: ILinkable<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) = linkThis.link(linkOther)
	}
}