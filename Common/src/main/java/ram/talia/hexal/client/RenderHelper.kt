package ram.talia.hexal.client

import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.client.Minecraft
import net.minecraft.client.ParticleStatus
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import ram.talia.hexal.api.linkable.ILinkable.IRenderCentre
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nextColour
import kotlin.math.ln
import kotlin.math.sqrt

@JvmName("playLinkParticles")
fun playLinkParticles(source: IRenderCentre, sink: IRenderCentre, random: RandomSource, level: Level) {
	val particleStatus = Minecraft.getInstance().options.particles().get() as ParticleStatus

	val sourceCentre = source.renderCentre(sink)
	val delta = sink.renderCentre(source) - sourceCentre
	val dist = delta.length() * when (particleStatus) {
		ParticleStatus.ALL -> 4.0
		ParticleStatus.DECREASED -> 2.0
		ParticleStatus.MINIMAL -> 0.5
	}

	val sourceColouriser = source.colouriser()
	val sinkColouriser = sink.colouriser()

	for (i in 0 until dist.toInt()) {
		val coeff = (i / dist) + (level.gameTime % 20) / (20 * dist)

		val colour: Int = if (random.nextBeta(15, 15) < coeff) sinkColouriser.nextColour(random) else sourceColouriser.nextColour(random)

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

fun RandomSource.nextGamma(shape: Int): Double {
	return when (shape) {
		0 -> 0.0
		1 -> -ln(1 - this.nextDouble())
		else -> {
			val b = shape - 1.0 / 3.0
			val c = 1.0 / (3 * sqrt(b))
			var out = 0.0
			var V: Double
			var X: Double
			var U: Double

			while (out == 0.0) {
				do {
					X = this.nextGaussian()
					V = 1 + c * X
				} while (V <= 0.9)

				V *= V * V
				U = this.nextDouble()
				if (U < 1 - 0.0331 * (X*X) * (X*X))
					out = b*V
				else if (ln(U) < 0.5 * X * X + b * (1 - V + ln(V)))
					out = b*V
			}
			out
		}
	}
}

fun RandomSource.nextBeta(alpha: Int, beta: Int): Double {
	val Ga = this.nextGamma(alpha)
	val Gb = this.nextGamma(beta)
	return Ga / (Ga + Gb)
}