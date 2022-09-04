package ram.talia.hexal.common.casting.actions.spells.link

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import kotlin.math.max

object OpSendIota : SpellOperator {
	private const val COST_SEND_IOTA = ManaConstants.DUST_UNIT
	override val argc = 2

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
//			throw MishapNoSpellCircle()
				return debugExecute(args, ctx)

		val linkedIndex = max(args.getChecked<Double>(0, argc).toInt(), 0)
		val iota = args[1]

		if (linkedIndex >= mCast.wisp.numLinked())
			throw MishapInvalidIota(
				linkedIndex.asSpellResult[0],
				0,
				TranslatableComponent("hexcasting.mishap.invalid_value.int.between", 0, mCast.wisp.numLinked())
			)

		val other = mCast.wisp.getLinked(linkedIndex)

		ctx.assertVecInRange(other.getPos())

		return Triple(
			Spell(other, iota),
			COST_SEND_IOTA,
			listOf(ParticleSpray.burst(other.getPos(), 1.5))
		)
	}

	fun debugExecute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val other = args.getChecked<Entity>(0, argc)
		val iota = args[1]

		if (other !is ILinkable<*>)
			throw MishapInvalidIota(
				other.asSpellResult[0],
				0,
				TranslatableComponent("hexcasting.mishap.invalid_value.int.between", 0, 5)
			)

		ctx.assertEntityInRange(other)

		return Triple(
			Spell(other, iota),
			COST_SEND_IOTA,
			listOf(ParticleSpray.burst(other.getPos(), 1.5))
		)
	}

	private data class Spell(val other: ILinkable<*>, val iota: SpellDatum<*>) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			HexalAPI.LOGGER.info("sending $iota to $other")
			other.receiveIota(iota)
		}
	}
}