package ram.talia.hexal.api

import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.IotaType
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.iota.Vec3Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.spell.iota.EntityTypeIota
import ram.talia.hexal.api.spell.iota.GateIota
import ram.talia.hexal.api.spell.iota.IotaTypeIota
import ram.talia.hexal.api.spell.iota.ItemIota
import ram.talia.hexal.api.spell.iota.ItemTypeIota
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.BaseWisp

operator fun Double.times(vec: Vec3): Vec3 = vec.scale(this)
operator fun Vec3.times(d: Double): Vec3 = this.scale(d)
operator fun Vec3.div(d: Double): Vec3 = this.scale(1/d)
operator fun Vec3.plus(vec3: Vec3): Vec3 = this.add(vec3)
operator fun Vec3.minus(vec3: Vec3): Vec3 = this.subtract(vec3)
operator fun Vec3.unaryMinus(): Vec3 = this.scale(-1.0)

/**
 * If the addition would overflow, instead bound it at MAX/MIN.
 */
fun Int.addBounded(int: Int): Int {
    return if (int > 0)
        if (this + int < this) Int.MAX_VALUE else this + int
    else
        if (this + int > this) Int.MIN_VALUE else this + int
}

fun <T, R> Iterable<T>.reductions(initial: R, operation: (acc: R, T) -> R) : Sequence<R> = sequence {
    var last = initial
    forEach {
        last = operation(last, it)
        yield(last)
    }
}

inline val IotaType<*>.asActionResult get() = listOf(IotaTypeIota(this))
inline val Block.asActionResult get() = listOf(ItemTypeIota(this))
inline val EntityType<*>.asActionResult get() = listOf(EntityTypeIota(this))
inline val Item.asActionResult get() = listOf(ItemTypeIota(this))


fun List<Iota>.getBaseWisp(idx: Int, argc: Int = 0): BaseWisp {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is BaseWisp)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.wisp")
}

fun List<Iota>.getBaseCastingWisp(idx: Int, argc: Int = 0): BaseCastingWisp {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is BaseCastingWisp)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "entity.wisp.casting")
}

fun List<Iota>.getVec3OrListVec3(idx: Int, argc: Int = 0): Either<Vec3, List<Vec3>> {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    return when (x) {
        is Vec3Iota -> Either.left(x.vec3)
        is ListIota -> {
            val out = mutableListOf<Vec3>()
            for (v in x.list) {
                if (v is Vec3Iota) {
                    out.add(v.vec3)
                } else {
                    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "veclist")
                }
            }
            Either.right(out)
        }
        else -> throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "veclist")
    }
}

fun List<Iota>.getItemType(idx: Int, argc: Int = 0): Item? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ItemTypeIota) {
        return x.item
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "type.item")
}

fun List<Iota>.getBlockType(idx: Int, argc: Int = 0): Block? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ItemTypeIota) {
        return x.block
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "type.block")
}

fun List<Iota>.getItemBlockType(idx: Int, argc: Int = 0): Either<Item, Block> {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ItemTypeIota) {
        return x.either
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "type.block")
}

fun List<Iota>.getGate(idx: Int, argc: Int = 0): GateIota {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is GateIota)
        return x

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "gate")
}

fun List<Iota>.getItem(idx: Int, argc: Int = 0): ItemIota {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ItemIota)
        return x

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "item")
}

fun List<Iota>.getBlockPosOrItem(idx: Int, argc: Int = 0): Either<BlockPos, ItemIota> {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }

    return when (x) {
        is Vec3Iota -> Either.left(BlockPos(x.vec3))
        is ItemIota -> Either.right(x)
        else -> throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "vecitem")
    }
}