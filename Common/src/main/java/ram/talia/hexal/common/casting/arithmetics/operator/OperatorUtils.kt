package ram.talia.hexal.common.casting.arithmetics.operator

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import ram.talia.hexal.api.casting.iota.MoteIota

fun Iterator<IndexedValue<Iota>>.nextMote(argc: Int = 0): MoteIota? {
    val (idx, x) = this.next()
    if (x is MoteIota)
        return x.selfOrNull()
    if (x is NullIota)
        return null

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "mote")
}

