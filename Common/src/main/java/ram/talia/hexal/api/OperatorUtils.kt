package ram.talia.hexal.api

import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.iota.*
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import com.mojang.datafixers.util.Either
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.mediafieditems.ItemRecord
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.api.spell.iota.EntityTypeIota
import ram.talia.hexal.api.spell.iota.GateIota
import ram.talia.hexal.api.spell.iota.IotaTypeIota
import ram.talia.hexal.api.spell.iota.MoteIota
import ram.talia.hexal.api.spell.iota.ItemTypeIota
import ram.talia.hexal.api.util.Anyone
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.BaseWisp
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToLong

operator fun Double.times(vec: Vec3): Vec3 = vec.scale(this)
operator fun Vec3.times(d: Double): Vec3 = this.scale(d)
operator fun Vec3.div(d: Double): Vec3 = this.scale(1/d)
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
inline val List<Item>.asActionResult get() = this.map { ItemTypeIota(it) }.asActionResult
inline val Map<MediafiedItemManager.Index, ItemRecord>.asActionResult get() = this.map { (index, _) -> MoteIota(index) }.asActionResult

fun ItemStack.asActionResult(storageUUID: UUID) = listOf(MoteIota.makeIfStorageLoaded(this, storageUUID) ?: NullIota())

fun List<Iota>.getBaseWisp(idx: Int, argc: Int = 0): BaseWisp {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is BaseWisp)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "wisp")
}

fun List<Iota>.getBaseCastingWisp(idx: Int, argc: Int = 0): BaseCastingWisp {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e is BaseCastingWisp)
            return e
    }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "wisp.casting")
}

fun List<Iota>.getBlockPosOrNull(idx: Int, argc: Int = 0): BlockPos? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is Vec3Iota)
        return BlockPos(x.vec3)
    if (x is NullIota)
        return null

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "vector")
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

fun List<Iota>.getStrictlyPositiveLong(idx: Int, argc: Int = 0): Long {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToLong()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded > 0) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.strictly_positive")
}

fun List<Iota>.getBlockPosOrItemEntityOrItem(idx: Int, argc: Int = 0): Anyone<BlockPos, ItemEntity, MoteIota>? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    return when (x) {
        is Vec3Iota -> Anyone.first(BlockPos(x.vec3))
        is EntityIota -> {
            if (x.entity.isRemoved)
                throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "entity.itemitemframe")
            (x.entity as? ItemEntity)?.let { Anyone.second(it) } ?: throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "blockitementityitem")
        }
        is MoteIota -> x.selfOrNull()?.let { Anyone.third(it) }
        is NullIota -> null
        else -> throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "blockitementityitem")
    }
}

fun List<Iota>.getItemEntityOrItemFrame(idx: Int, argc: Int = 0): Either<ItemEntity, ItemFrame> {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityIota) {
        val e = x.entity
        if (e.isRemoved)
            throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "entity.itemitemframe")
        if (e is ItemEntity)
            return Either.left(e)
        if (e is ItemFrame)
            return Either.right(e)
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "entity.itemitemframe")
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
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "type.item")
}

fun List<Iota>.getBlockTypeOrBlockItem(idx: Int, argc: Int = 0): Either<Block, MoteIota>? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is ItemTypeIota)
        return x.block ?.let { Either.left(it) }
    if (x is MoteIota)
        return x.selfOrNull()?.let { if (it.item is BlockItem) Either.right(it) else null }
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "type.block")
}

fun List<Iota>.getEntityType(idx: Int, argc: Int = 0): EntityType<*> {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is EntityTypeIota)
        return x.entityType
    if (x is EntityIota)
        return x.entity.type
    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "type.entity")
}

fun List<Iota>.getGate(idx: Int, argc: Int = 0): GateIota {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is GateIota)
        return x

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "gate")
}

fun List<Iota>.getMote(idx: Int, argc: Int = 0): MoteIota? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is MoteIota)
        return x.selfOrNull()
    if (x is NullIota)
        return null

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "mote")
}

fun List<Iota>.getMoteOrItemEntityOrItemFrame(idx: Int, argc: Int = 0): Anyone<MoteIota, ItemEntity, ItemFrame>? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is MoteIota)
        return x.selfOrNull()?.let { Anyone.first(it) }
    if (x is NullIota)
        return null
    if (x is EntityIota) {
        val e = x.entity
        if (e is ItemEntity)
            return Anyone.second(e)
        if (e is ItemFrame)
            return Anyone.third(e)
    }

    throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "moteentity.itemitemframe")
}

fun List<Iota>.getMoteOrItemType(idx: Int, argc: Int = 0): Either<MoteIota, Item>? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is MoteIota)
        return x.selfOrNull()?.let { Either.left(it) }
    if (x is ItemTypeIota)
        return Either.right(x.item)
    if (x is NullIota)
        return null

    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "moteitemtype")
}

fun List<Iota>.getMoteOrList(idx: Int, argc: Int = 0): Either<MoteIota, SpellList>? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    return when (x) {
        is MoteIota -> Either.left(x)
        is NullIota -> null
        is ListIota -> Either.right(x.list)
        else -> throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "motemotelistmotelistlist")
    }
}

fun List<Iota>.getMoteOrMoteList(idx: Int, argc: Int = 0): Either<MoteIota, List<MoteIota>>? {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    return when (x) {
        is MoteIota -> Either.left(x)
        is NullIota -> null
        is ListIota -> {
            val out = mutableListOf<MoteIota>()
            for (i in x.list) {
                when (i) {
                    is MoteIota -> out.add(i)
                    is NullIota -> continue
                    else -> throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "motemotelist")
                }
            }
            Either.right(out)
        }
        else -> throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "motemotelist")
    }
}