package ram.talia.hexal.api

import net.minecraft.world.phys.Vec3

operator fun Double.times(vec: Vec3): Vec3 = vec.scale(this)
operator fun Vec3.times(d: Double): Vec3 = this.scale(d)
operator fun Vec3.div(d: Double): Vec3 = this.scale(1 / d)
operator fun Vec3.plus(vec3: Vec3): Vec3 = this.add(vec3)
operator fun Vec3.minus(vec3: Vec3): Vec3 = this.subtract(vec3)
operator fun Vec3.unaryMinus(): Vec3 = this.scale(-1.0)

/**
 * If the addition would overflow, instead bound it at MAX/MIN.
 * https://codereview.stackexchange.com/questions/92686/saturated-arithmetic
 */
fun Int.addBounded(int: Int): Int {
    // Sum ignoring overflow/underflow
    val sum: Int = this + int


    // Long.MIN_VALUE if result positive (potential underflow)
    // Long.MAX_VALUE if result negative (potential overflow)
    val limit = Int.MIN_VALUE xor (sum shr 31)


    // -1 if overflow/underflow occurred, 0 otherwise
    val overflow: Int = ((this xor sum) and (this xor int).inv()) shr 31


    // limit if overflowed/underflowed, else s
    return ((limit xor sum) and overflow) xor sum
}

/**
 * If the multiplication would overflow, instead bound it at MAX/MIN.
 * https://codereview.stackexchange.com/questions/92686/saturated-arithmetic
 */
fun Int.mulBounded(int: Int): Int {
    val result: Int = this * int

    // See https://goo.gl/ZMEZEa
    val nlz = (Integer.numberOfLeadingZeros(this) + Integer.numberOfLeadingZeros(this.inv())
            + Integer.numberOfLeadingZeros(int) + Integer.numberOfLeadingZeros(int.inv()))
    if (nlz > 33) return result
    if (nlz < 32) return saturate(this xor int)
    if ((this == Int.MIN_VALUE) && (int < 0)) return Int.MAX_VALUE
    if (int != 0 && result / int != this) return saturate(this xor int)
    return result
}

private fun saturate(sign: Int): Int {
    return if (sign > 0) Int.MAX_VALUE else Int.MIN_VALUE
}

/**
 * If the addition would overflow, instead bound it at MAX/MIN.
 * https://codereview.stackexchange.com/questions/92686/saturated-arithmetic
 */
fun Long.addBounded(long: Long): Long {
    // Sum ignoring overflow/underflow
    val sum: Long = this + long


    // Long.MIN_VALUE if result positive (potential underflow)
    // Long.MAX_VALUE if result negative (potential overflow)
    val limit = Long.MIN_VALUE xor (sum shr 63)


    // -1 if overflow/underflow occurred, 0 otherwise
    val overflow: Long = ((this xor sum) and (this xor long).inv()) shr 63


    // limit if overflowed/underflowed, else s
    return ((limit xor sum) and overflow) xor sum
}

/**
 * If the multiplication would overflow, instead bound it at MAX/MIN.
 * https://codereview.stackexchange.com/questions/92686/saturated-arithmetic
 */
fun Long.mulBounded(long: Long): Long {
    val result: Long = this * long

    // See https://goo.gl/ZMEZEa
    val nlz = (java.lang.Long.numberOfLeadingZeros(this) + java.lang.Long.numberOfLeadingZeros(this.inv())
            + java.lang.Long.numberOfLeadingZeros(long) + java.lang.Long.numberOfLeadingZeros(long.inv()))
    if (nlz > 65) return result
    if (nlz < 64) return saturate(this xor long)
    if ((this == Long.MIN_VALUE) && (long < 0)) return Long.MAX_VALUE
    if (long != 0L && result / long != this) return saturate(this xor long)
    return result
}

fun Long.toIntCapped(): Int {
    return if (this <= Int.MIN_VALUE) {
        Int.MIN_VALUE
    } else if (this >= Int.MAX_VALUE) {
        Int.MAX_VALUE
    } else {
        toInt()
    }
}

private fun saturate(sign: Long): Long {
    return if (sign > 0) Long.MAX_VALUE else Long.MIN_VALUE
}

fun <T, R> Iterable<T>.reductions(initial: R, operation: (acc: R, T) -> R): Sequence<R> = sequence {
    var last = initial
    forEach {
        last = operation(last, it)
        yield(last)
    }
}