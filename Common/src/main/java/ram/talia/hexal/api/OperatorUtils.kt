package ram.talia.hexal.api

import net.minecraft.world.phys.Vec3

operator fun Double.times(vec: Vec3) = vec.scale(this)
operator fun Vec3.times(d: Double) = this.scale(d)
operator fun Vec3.div(d: Double): Vec3 = this.scale(1/d)
operator fun Vec3.plus(vec3: Vec3) = this.add(vec3)
operator fun Vec3.minus(vec3: Vec3) = this.subtract(vec3)

operator fun Vec3.unaryMinus() = this.scale(-1.0)