package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
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
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus


open class BaseWisp : Projectile {
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

		setPos(position() + vel)

		if(level.isClientSide) {
			playParticles();
		}
	}

	/**
	 * Set the look vector of the wisp equal to its movement direction
	 */
	fun setLookVector(vel: Vec3) {
		if (xRotO == 0.0f && yRotO == 0.0f) {
			val horizontalDistance = vel.horizontalDistance()
			yRot = (Mth.atan2(vel.x, vel.z) * 57.2957763671875).toFloat()
			xRot = (Mth.atan2(vel.y, horizontalDistance) * 57.2957763671875).toFloat()
			yRotO = yRot
			xRotO = xRot
		}
	}

	fun getHitResult(start: Vec3, end: Vec3): BlockHitResult = level.clip(ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))

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
		val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))

		val delta = position() - oldPos
		val dist = delta.length() * 6

		for (i in 0..dist.toInt()) {
			val colour: Int = colouriser.getColor(
				random.nextFloat() * 16384,
				Vec3(
					random.nextFloat().toDouble(),
					random.nextFloat().toDouble(),
					random.nextFloat().toDouble()
				).scale((random.nextFloat() * 3).toDouble())
			)

			val coeff = i / dist
			level.addParticle(
				ConjureParticleOptions(colour, false),
				(oldPos.x + delta.x * coeff),
				(oldPos.y + delta.y * coeff),
				(oldPos.z + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5))
		}
	}

	fun setColouriser(colouriser: FrozenColorizer) {
		entityData.set(COLOURISER, colouriser.serializeToNBT())
	}
	override fun load(compound: CompoundTag)
	{
		// assuming this is for saving/loading chunks and the game
		super.load(compound)
		entityData.set(COLOURISER, compound.getCompound(TAG_COLOURISER))
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)
		compound.putCompound(TAG_COLOURISER, entityData.get(COLOURISER))
	}
	override fun defineSynchedData() {
		// defines the entry in SynchedEntityData associated with the EntityDataAccessor COLOURISER, and gives it a default value
		entityData.define(COLOURISER, FrozenColorizer.DEFAULT.get().serializeToNBT())
	}

	companion object {
		@JvmField
		val COLOURISER: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)
		const val TAG_COLOURISER = "tag_colouriser"
	}
}

