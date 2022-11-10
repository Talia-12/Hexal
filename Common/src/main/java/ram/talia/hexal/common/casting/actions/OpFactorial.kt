package ram.talia.hexal.common.casting.actions

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import ram.talia.hexal.api.HexalAPI
import kotlin.math.*

object OpFactorial : ConstManaAction {
	override val argc = 1

	private fun factorial(number: Int): Long {
		var result: Long = 1

		for (factor in 2..number) {
			result *= factor
		}

		return result
	}

	// https://introcs.cs.princeton.edu/java/91float/Gamma.java.html
	private fun logGamma(x: Double): Double {
		val ser =  ( 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
		                 + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
		                 +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5) )

		return (x - 0.5) * ln(x + 4.5) - (x + 4.5) + ln(ser * sqrt(2*PI))
	}

	fun gamma(x: Double): Double {
		return exp(logGamma(x))
	}

	override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
		val arg = args.getDouble(0, argc)
		val argInt = arg.roundToInt()
		if (argInt >= 0 && DoubleIota.tolerates(arg, argInt.toDouble())) {
			return factorial(argInt).asActionResult
		}

		return gamma(arg + 1).asActionResult
	}
}