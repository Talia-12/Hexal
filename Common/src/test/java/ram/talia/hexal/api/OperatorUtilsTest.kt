package ram.talia.hexal.api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random

class OperatorUtilsTest {
    @Test
    fun testIntAddBounded() {
        val random = Random(124912)

        for (i in 0..(10.toFloat().pow(6).toInt())) {
            val inta = random.nextInt()
            val intb = random.nextInt()

            val sSum = inta.addBounded(intb)

            val trueSSum = try { Math.addExact(inta, intb) } catch (_: ArithmeticException) { if (sign(inta.toFloat()) > 0) Int.MAX_VALUE else Int.MIN_VALUE }

            assertEquals(trueSSum, sSum, "adding $inta and $intb, expected $trueSSum, got $sSum")
        }
    }

    @Test
    fun testIntMulBounded() {
        val random = Random(124912)

        for (i in 0..(10.toFloat().pow(6).toInt())) {
            val inta = random.nextInt()
            val intb = random.nextInt()

            val sMul = inta.mulBounded(intb)

            val trueSMul = try { Math.multiplyExact(inta, intb) } catch (_: ArithmeticException) { if (sign(inta.toFloat()) * sign(intb.toFloat()) > 0) Int.MAX_VALUE else Int.MIN_VALUE }

            assertEquals(trueSMul, sMul, "multiplying $inta and $intb, expected $trueSMul, got $sMul")
        }
    }

    @Test
    fun testLongAddBounded() {
        val random = Random(124912)

        for (i in 0..(10.toFloat().pow(6).toInt())) {
            val longa = random.nextLong()
            val longb = random.nextLong()

            val sSum = longa.addBounded(longb)

            val trueSSum = try { Math.addExact(longa, longb) } catch (_: ArithmeticException) { if (sign(longa.toFloat()) > 0) Long.MAX_VALUE else Long.MIN_VALUE }

            assertEquals(trueSSum, sSum, "adding $longa and $longb, expected $trueSSum, got $sSum")
        }
    }

    @Test
    fun testLongMulBounded() {
        val random = Random(124912)

        for (i in 0..(10.toFloat().pow(6).toInt())) {
            val longa = random.nextLong()
            val longb = random.nextLong()

            val sMul = longa.mulBounded(longb)

            val trueSMul = try { Math.multiplyExact(longa, longb) } catch (_: ArithmeticException) { if (sign(longa.toFloat()) * sign(longb.toFloat()) > 0) Long.MAX_VALUE else Long.MIN_VALUE }

            assertEquals(trueSMul, sMul, "multiplying $longa and $longb, expected $trueSMul, got $sMul")
        }
    }
}