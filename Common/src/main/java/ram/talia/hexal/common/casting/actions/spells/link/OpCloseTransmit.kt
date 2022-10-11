package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern
import ram.talia.hexal.api.spell.casting.IMixinCastingContext

object OpCloseTransmit : ConstManaOperator {
	@JvmField
	val PATTERN = HexPattern.fromAngles("ewaqawe", HexDir.EAST)

	override val argc = 0

	@Suppress("CAST_NEVER_SUCCEEDS", "KotlinConstantConditions")
	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		val mCtx = ctx as? IMixinCastingContext
		mCtx?.resetForwardingTo()
		return listOf()
	}
}