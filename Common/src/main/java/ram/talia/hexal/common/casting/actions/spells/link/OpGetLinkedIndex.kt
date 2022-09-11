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

object OpGetLinkedIndex : ConstManaOperator {
	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		val linked = args.getChecked<Entity>(0, argc)

		if (linked !is ILinkable<*>)
			return (-1).asSpellResult

		return mCast.wisp.getLinkedIndex(linked).asSpellResult
	}
}