package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3

object OpFreeze : SpellOperator {
	const val FREEZE_COST = 3 * ManaConstants.DUST_UNIT

	override val argc = 1

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
		val toFreeze = Vec3.atCenterOf(BlockPos(args.getChecked<Vec3>(0, argc)))

		ctx.assertVecInRange(toFreeze)

		return Triple(
			Spell(toFreeze),
			FREEZE_COST,
			listOf(ParticleSpray.burst(toFreeze, 1.0))
		)
	}

	private data class Spell(val vec: Vec3) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			val recman = ctx.world.recipeManager
			val recipes = recman.getAllRecipesFor(HexalRecipeSerializers.FREEZE_TYPE)
		}
	}
}