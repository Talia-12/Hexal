package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus

class ProjectileWisp : BaseWisp {
	var isAffectedByGravity = true

	var onCollisionHex: List<SpellDatum<*>> = ArrayList()

	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)
	constructor(world: Level, pos: Vec3, caster: Player, media: Int) : super(world, pos, caster, media)

	override fun move() {
		setLookVector(deltaMovement)

		if (isAffectedByGravity)
			deltaMovement -= Vec3(0.0, 0.05, 0.0)

		traceAnyHit(position(), position() + deltaMovement)

		setPos(position() + deltaMovement)
	}

	override fun onHitEntity(result: EntityHitResult) {
		super.onHitEntity(result)

		setPos(result.location)
		if (level.isClientSide)
			playParticles()

		castSpell(onCollisionHex, listOf(SpellDatum.make(this), SpellDatum.make(result.entity)).toMutableList())

		discard()
	}

	override fun onHitBlock(result: BlockHitResult) {
		super.onHitBlock(result)

		setPos(result.location)
		if (level.isClientSide)
			playParticles()

		castSpell(onCollisionHex, listOf(SpellDatum.make(this), SpellDatum.make(Vec3.atCenterOf(result.blockPos))).toMutableList())

		discard()
	}
}