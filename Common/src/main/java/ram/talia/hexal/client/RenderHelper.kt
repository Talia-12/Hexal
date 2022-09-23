package ram.talia.hexal.client

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.world.level.Level
import ram.talia.hexal.api.linkable.ILinkable.IRenderCentre
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nextColour
import java.util.*

@JvmName("playLinkParticles")
fun playLinkParticles(source: IRenderCentre, sink: IRenderCentre, colouriser: FrozenColorizer, random: Random, level: Level) {
	val sourceCentre = source.renderCentre()
	val delta = sink.renderCentre() - sourceCentre
	val dist = delta.length() * 12

	for (i in 0..dist.toInt()) {
		val colour: Int = colouriser.nextColour(random)

		val coeff = i / dist
		level.addParticle(
			ConjureParticleOptions(colour, false),
			(sourceCentre.x + delta.x * coeff),
			(sourceCentre.y + delta.y * coeff),
			(sourceCentre.z + delta.z * coeff),
			0.0125 * (random.nextDouble() - 0.5),
			0.0125 * (random.nextDouble() - 0.5),
			0.0125 * (random.nextDouble() - 0.5)
		)
	}
}