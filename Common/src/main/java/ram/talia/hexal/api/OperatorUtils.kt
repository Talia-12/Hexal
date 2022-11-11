package ram.talia.hexal.api

import at.petrak.hexcasting.api.spell.iota.IotaType
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.spell.iota.BlockTypeIota
import ram.talia.hexal.api.spell.iota.EntityTypeIota
import ram.talia.hexal.api.spell.iota.IotaTypeIota
import ram.talia.hexal.api.spell.iota.ItemTypeIota

operator fun Double.times(vec: Vec3) = vec.scale(this)
operator fun Vec3.times(d: Double) = this.scale(d)
operator fun Vec3.div(d: Double): Vec3 = this.scale(1/d)
operator fun Vec3.plus(vec3: Vec3) = this.add(vec3)
operator fun Vec3.minus(vec3: Vec3) = this.subtract(vec3)
operator fun Vec3.unaryMinus() = this.scale(-1.0)

inline val IotaType<*>.asActionResult get() = listOf(IotaTypeIota(this))
inline val Block.asActionResult get() = listOf(BlockTypeIota(this))
inline val EntityType<*>.asActionResult get() = listOf(EntityTypeIota(this))
inline val Item.asActionResult get() = listOf(ItemTypeIota(this))