package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.xplat.IXplatAbstractions

object OpGetLinkedIndex : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		val linkThis: ILinkable<*> = when (val wisp = mCast?.wisp) {
			null -> IXplatAbstractions.INSTANCE.getLinkstore(ctx.caster)
			else -> wisp
		}

		val linked = args.getChecked<Entity>(0, argc)

		if (linked !is ILinkable<*>)
			return (-1).asSpellResult

		return linkThis.getLinkedIndex(linked).asSpellResult
	}
}