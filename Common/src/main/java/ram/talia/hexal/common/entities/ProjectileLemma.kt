package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.LemmaCastingManager

class ProjectileLemma : BaseLemma {
	var isAffectedByGravity = true

	constructor(entityType: EntityType<out BaseLemma>, world: Level) : super(entityType, world)
	constructor(entityType: EntityType<out ProjectileLemma>, world: Level, pos: Vec3, vel: Vec3, caster: Player, media: Int) : super(entityType, world, pos, caster, media) {
		velocity = vel
	}
	constructor(world: Level, pos: Vec3, vel: Vec3, caster: Player, media: Int) : super(HexalEntities.PROJECTILE_LEMMA, world, pos, caster, media) {
		velocity = vel
	}

	override fun move() {
		setLookVector(velocity)

		if (isAffectedByGravity)
			addVelocity(Vec3(0.0, -0.05, 0.0))

		traceAnyHit(position(), position() + velocity)

		setPos(position() + velocity)
	}

	override fun maxSqrCastingDistance() = 4.0

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

	override fun castCallback(result: LemmaCastingManager.LemmaCastResult) {
		discard()
	}

	//TODO: setup actually *saving* onCollisionHex

	companion object {
		const val CASTING_SCHEDULE_PRIORITY = 0
	}
}