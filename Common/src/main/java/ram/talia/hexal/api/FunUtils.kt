package ram.talia.hexal.api

import at.petrak.hexcasting.api.misc.FrozenColorizer
import net.minecraft.world.phys.Vec3
import java.util.Random

fun FrozenColorizer.nextColour(random: Random): Int {
	return getColor(
		random.nextFloat() * 16384,
		Vec3(
			random.nextFloat().toDouble(),
			random.nextFloat().toDouble(),
			random.nextFloat().toDouble()
		).scale((random.nextFloat() * 3).toDouble())
	)
}