package ram.talia.hexal.api.util

abstract class Anyone<A, B, C> {
    abstract val isA: Boolean
    abstract val isB: Boolean
    abstract val isC: Boolean

    open val a: A? = null
    open val b: B? = null
    open val c: C? = null

    abstract fun <D, E, F> map(aMap: (a: A) -> D, bMap: (b: B) -> E, cMap: (c: C) -> F): Anyone<D, E, F>
    abstract fun <D> flatMap(aMap: (a: A) -> D, bMap: (b: B) -> D, cMap: (c: C) -> D): D

    private class First<A, B, C>(override val a: A) : Anyone<A, B, C>() {
        override val isA = true
        override val isB = false
        override val isC = false

        override fun <D, E, F> map(aMap: (a: A) -> D, bMap: (b: B) -> E, cMap: (c: C) -> F): Anyone<D, E, F> {
            return First(aMap(a))
        }
        override fun <D> flatMap(aMap: (a: A) -> D, bMap: (b: B) -> D, cMap: (c: C) -> D): D {
            return aMap(a)
        }
    }

    private class Second<A, B, C>(override val b: B) : Anyone<A, B, C>() {
        override val isA = false
        override val isB = true
        override val isC = false

        override fun <D, E, F> map(aMap: (a: A) -> D, bMap: (b: B) -> E, cMap: (c: C) -> F): Anyone<D, E, F> {
            return Second(bMap(b))
        }
        override fun <D> flatMap(aMap: (a: A) -> D, bMap: (b: B) -> D, cMap: (c: C) -> D): D {
            return bMap(b)
        }
    }

    private class Third<A, B, C>(override val c: C) : Anyone<A, B, C>() {
        override val isA = false
        override val isB = false
        override val isC = true

        override fun <D, E, F> map(aMap: (a: A) -> D, bMap: (b: B) -> E, cMap: (c: C) -> F): Anyone<D, E, F> {
            return Third(cMap(c))
        }
        override fun <D> flatMap(aMap: (a: A) -> D, bMap: (b: B) -> D, cMap: (c: C) -> D): D {
            return cMap(c)
        }
    }

    companion object {
        fun <A, B, C>first(a: A): Anyone<A, B, C> = First(a)
        fun <A, B, C>second(b: B): Anyone<A, B, C> = Second(b)
        fun <A, B, C>third(c: C): Anyone<A, B, C> = Third(c)
    }
}