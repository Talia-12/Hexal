package ram.talia.hexal.common.casting.actions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ram.talia.hexal.common.casting.actions.OpFactorial.gamma
import kotlin.math.*

internal class GammaApproximateTest {
	@Test
	fun testPartialGamma() {
		for (i in 0..50) {
			val x = 1.0 + i/10.0

			val ser =  (1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
											+ 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
											+  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5) )

			val expected = (x + 4.5).pow(x - 0.5) * exp(-x - 4.5) * sqrt(2* PI) * ser
			val lnExpected = (x - 0.5) * ln(x + 4.5) - (x + 4.5) + ln(ser * sqrt(2*PI))
			println("$x, $expected, ${exp(lnExpected)} ($ser, $lnExpected)")
			assertEquals(expected, exp(lnExpected), 0.0001)
		}
	}

	@Test
	fun testGamma() {
		assertEquals(1.0, gamma(1.0), 0.0001)
		assertEquals(1.0, gamma(2.0), 0.0001)
		assertEquals(2.0, gamma(3.0), 0.0001)
		assertEquals(6.0, gamma(4.0), 0.0001)
		assertEquals(24.0, gamma(5.0), 0.0001)
		assertEquals(120.0, gamma(6.0), 0.0001)
	}
}