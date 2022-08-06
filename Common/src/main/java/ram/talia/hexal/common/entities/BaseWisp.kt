package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.ParticleSpray
import net.minecraft.client.particle.Particle
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus


class BaseWisp : Projectile {
	var isAffectedByGravity = true

	private var lifespan = 20 // how long the wisp has left to live, in ticks

	private var oldPos: Vec3 = position()

	fun addLifespan(dLifespan: Int) {
		lifespan += dLifespan
	}

	// error here isn't actually a problem
	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)

	constructor(world: Level, pos: Vec3) : super(HexalEntities.BASE_WISP, world) {
		setPos(pos)
	}

	constructor(world: Level, pos: Vec3, shooter: LivingEntity) : super(HexalEntities.BASE_WISP, world) {
		setPos(pos)
		owner = shooter
	}

	constructor(world: Level, pos: Vec3, shooter: LivingEntity, lifespan: Int) : super(HexalEntities.BASE_WISP, world) {
		setPos(pos)
		owner = shooter
		this.lifespan = lifespan
	}

	override fun tick() {
		super.tick()

		// check if lifespan is < 0 ; destroy the wisp if it is, decrement the lifespan otherwise.
		if (lifespan-- <= 0) discard()

		oldPos = position()
		var vel = deltaMovement

		setLookVector(vel)

		if (isAffectedByGravity)
			vel += Vec3(0.0, 0.05000000074505806, 0.0)

		traceAnyHit(position(), position() + vel)

		if(level.isClientSide) {
			playParticles();
		}

		setPos(position() + vel)
	}

	fun setLookVector(vel: Vec3) {
		// set the look vector of the wisp equal to its movement direction
		if (xRotO == 0.0f && yRotO == 0.0f) {
			val horizontalDistance = vel.horizontalDistance()
			yRot = (Mth.atan2(vel.x, vel.z) * 57.2957763671875).toFloat()
			xRot = (Mth.atan2(vel.y, horizontalDistance) * 57.2957763671875).toFloat()
			yRotO = yRot
			xRotO = xRot
		}
	}

	public fun getHitResult(start: Vec3, end: Vec3) = level.clip(ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))

	protected fun findHitEntity(start: Vec3, end: Vec3): EntityHitResult? =
		ProjectileUtil.getEntityHitResult(
			level,
			this,
			start,
			end,
			boundingBox.expandTowards(deltaMovement).inflate(1.0),
			this::canHitEntity
		)

	public fun traceAnyHit(start: Vec3, end: Vec3) {
		traceAnyHit(getHitResult(start, end), start, end)
	}

	public fun traceAnyHit(raytraceResult: HitResult?, start: Vec3, end: Vec3) {
		var tEnd = end


		if (raytraceResult != null && raytraceResult.type != HitResult.Type.MISS) {
			tEnd = raytraceResult.location
		}

		// get any entities in between the start location and tEnd, which is either the
		// first location on the line start-end intersecting a block, or end.
		val entityRaytraceResult = findHitEntity(start, tEnd)

		val tRaytraceResult = entityRaytraceResult ?: raytraceResult

		//TODO: Figure out best way to keep !ForgeEventFactory.onProjectileImpact(this, tRaytraceResult)
		if (tRaytraceResult != null && tRaytraceResult.type != HitResult.Type.MISS) {
			onHit(tRaytraceResult)
			hasImpulse = true
		}
	}

	override fun onHitEntity(result: EntityHitResult) {
		super.onHitEntity(result)

		discard()
	}

	override fun onHitBlock(result: BlockHitResult) {
		super.onHitBlock(result)

		discard()
	}

	protected fun playParticles() {
		val delta = position() - oldPos
		val dist = delta.length() * 6

		for (i in 0..dist.toInt()) {
			val coeff = i / dist
			level.addParticle(ParticleTypes.ENTITY_EFFECT,
				(xo + delta.x * coeff),
				(yo + delta.y * coeff) + 0.1,
				(zo + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5))
		}
	}

	override fun defineSynchedData() {}
}

