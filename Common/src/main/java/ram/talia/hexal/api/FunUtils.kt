package ram.talia.hexal.api

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.Vec3

fun FrozenColorizer.nextColour(random: RandomSource): Int {
	return getColor(
		random.nextFloat() * 16384,
		Vec3(
			random.nextFloat().toDouble(),
			random.nextFloat().toDouble(),
			random.nextFloat().toDouble()
		).scale((random.nextFloat() * 3).toDouble())
	)
}

fun RandomSource.nextDouble(lower: Double, upper: Double) = lower + (upper - lower) * this.nextDouble()

fun RandomSource.nextGaussian(mean: Double, stdev: Double) = mean + stdev * this.nextGaussian()

fun CastingContext.assertVecListInRange(list: List<Vec3>) = this.assertVecListInRange(list, null)

fun CastingContext.assertVecListInRange(list: List<Vec3>, intraRange: Double?) {
	for (vec in list) {
		this.assertVecInRange(vec)
	}
	if (intraRange != null) {
		val sqrRange = intraRange * intraRange

		for (i in list.indices) {
			for (j in i until list.size) {
				if (list[i].distanceToSqr(list[j]) > sqrRange) throw MishapLocationTooFarAway(list[j])
			}
		}
	}
}