package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.times
import ram.talia.hexal.common.lib.HexalEntities

class ProjectileWisp : BaseCastingWisp {
	var isAffectedByGravity = true

	constructor(entityType: EntityType<out BaseCastingWisp>, world: Level) : super(entityType, world)
	constructor(entityType: EntityType<out ProjectileWisp>, world: Level, pos: Vec3, vel: Vec3, caster: Player, media: Int) : super(entityType, world, pos, caster, media) {
		velocity = vel
	}
	constructor(world: Level, pos: Vec3, vel: Vec3, caster: Player, media: Int) : super(HexalEntities.PROJECTILE_WISP, world, pos, caster, media) {
		velocity = vel
	}

	override fun move() {
		if (isAffectedByGravity)
			addVelocityScaled(Vec3(0.0, -0.05, 0.0))

		// either [position] + [velocity] if there was nothing in between the two points of the
		// trace, or the collision point if there was.
		val endPos = traceAnyHit(position(), position() + velocity)

		setPos(endPos)
	}

	override fun maxSqrCastingDistance() = CASTING_RADIUS * CASTING_RADIUS

	fun getHitResult(start: Vec3, end: Vec3): BlockHitResult = level.clip(ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))

	protected fun findHitEntity(start: Vec3, end: Vec3): EntityHitResult? =
		ProjectileUtil.getEntityHitResult(
			level,
			this,
			start,
			end,
			boundingBox.expandTowards(velocity).inflate(1.0),
			this::canHitEntity
		)

	protected fun canHitEntity(entity: Entity): Boolean {
		return if (!entity.isSpectator && entity.isAlive && entity.isPickable) {
			caster == null || !caster!!.isPassengerOfSameVehicle(entity)
		} else {
			false
		}
	}

	public fun traceAnyHit(start: Vec3, end: Vec3): Vec3 {
		return traceAnyHit(getHitResult(start, end), start, end)
	}

	public fun traceAnyHit(raytraceResult: HitResult?, start: Vec3, end: Vec3): Vec3 {
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

		return tEnd
	}

	fun onHit(result: HitResult) {
		val type = result.type
		if (type == HitResult.Type.ENTITY) {
			onHitEntity((result as EntityHitResult))
		} else if (type == HitResult.Type.BLOCK) {
			onHitBlock((result as BlockHitResult))
		}
		if (type != HitResult.Type.MISS) {
			this.gameEvent(GameEvent.PROJECTILE_LAND, this.caster)
		}
	}
	fun onHitEntity(result: EntityHitResult) {
		setPos(result.location)
		if (level.isClientSide)
			playTrailParticles()
		else
			scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, mutableListOf(SpellDatum.make(this), SpellDatum.make(result.entity)))
	}

	fun onHitBlock(result: BlockHitResult) {
		setPos(result.location)
		if (level.isClientSide)
			playTrailParticles()
		else
			scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, mutableListOf(SpellDatum.make(this), SpellDatum.make(Vec3.atCenterOf(result.blockPos))))
	}

	override fun castCallback(result: WispCastingManager.WispCastResult) {
		super.castCallback(result)
		discard()
	}

	companion object {
		const val CASTING_SCHEDULE_PRIORITY = 0
		const val CASTING_RADIUS = 4.0
	}
}