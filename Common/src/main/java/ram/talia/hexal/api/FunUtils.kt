package ram.talia.hexal.api

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.Vec3

fun FrozenPigment.nextColour(random: RandomSource): Int {
	return colorProvider.getColor(
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

fun CastingEnvironment.assertVecListInRange(list: List<Vec3>) = this.assertVecListInRange(list, null)

fun CastingEnvironment.assertVecListInRange(list: List<Vec3>, intraRange: Double?) {
	for (vec in list) {
		this.assertVecInRange(vec)
	}
	if (intraRange != null) {
		val sqrRange = intraRange * intraRange

		for (i in list.indices) {
			for (j in i until list.size) {
				if (list[i].distanceToSqr(list[j]) > sqrRange) throw MishapBadLocation(list[j])
			}
		}
	}
}