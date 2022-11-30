package ram.talia.hexal.api

import at.petrak.hexcasting.api.misc.FrozenColorizer
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