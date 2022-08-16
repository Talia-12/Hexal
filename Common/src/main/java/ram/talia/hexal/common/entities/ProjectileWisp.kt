package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.WispCastingManager

class ProjectileWisp : BaseWisp {
	var isAffectedByGravity = true

	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)
	constructor(entityType: EntityType<out ProjectileWisp>, world: Level, pos: Vec3, caster: Player, media: Int) : super(entityType, world, pos, caster, media)
	constructor(world: Level, pos: Vec3, caster: Player, media: Int) : super(HexalEntities.PROJECTILE_WISP, world, pos, caster, media)

	override fun move() {
		setLookVector(deltaMovement)

		if (isAffectedByGravity)
			addVelocity(Vec3(0.0, -0.05, 0.0))

		traceAnyHit(position(), position() + deltaMovement)

		setPos(position() + deltaMovement)
	}

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

	//TODO: setup actually *saving* onCollisionHex

	companion object {
		const val CASTING_SCHEDULE_PRIORITY = 0
	}
}