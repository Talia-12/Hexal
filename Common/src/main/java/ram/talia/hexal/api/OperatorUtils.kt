package ram.talia.hexal.api

import net.minecraft.world.phys.Vec3

operator fun Vec3.plus(vec3: Vec3) = this.add(vec3)
operator fun Vec3.minus(vec3: Vec3) = this.subtract(vec3)