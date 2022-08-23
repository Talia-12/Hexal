package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.times

class ProjectileWisp : BaseWisp {
	var isAffectedByGravity = true

	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)
	constructor(entityType: EntityType<out ProjectileWisp>, world: Level, pos: Vec3, vel: Vec3, caster: Player, media: Int) : super(entityType, world, pos, caster, media) {
		velocity = vel * START_VEL_MAGNITUDE
	}
	constructor(world: Level, pos: Vec3, vel: Vec3, caster: Player, media: Int) : super(HexalEntities.PROJECTILE_WISP, world, pos, caster, media) {
		velocity = vel * START_VEL_MAGNITUDE
	}

	override fun move() {
		setLookVector(velocity)

		if (isAffectedByGravity)
			addVelocity(Vec3(0.0, -0.05, 0.0))

		// either [position] + [velocity] if there was nothing in between the two points of the
		// trace, or the collision point if there was.
		val endPos = traceAnyHit(position(), position() + velocity)

		setPos(endPos)
	}

	override fun maxSqrCastingDistance() = CASTING_RADIUS * CASTING_RADIUS

	override fun onHitEntity(result: EntityHitResult) {
		super.onHitEntity(result)

		setPos(result.location)
		if (level.isClientSide)
			playParticles()

		scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, listOf(SpellDatum.make(this), SpellDatum.make(result.entity)).toMutableList())
	}

	override fun onHitBlock(result: BlockHitResult) {
		super.onHitBlock(result)

		setPos(result.location)
		if (level.isClientSide)
			playParticles()

		scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, listOf(SpellDatum.make(this), SpellDatum.make(Vec3.atCenterOf(result.blockPos))).toMutableList())
	}

	override fun castCallback(result: WispCastingManager.WispCastResult) {
		discard()
	}

	companion object {
		const val CASTING_SCHEDULE_PRIORITY = 0
		const val START_VEL_MAGNITUDE = 1.75
		const val CASTING_RADIUS = 4.0
	}
}