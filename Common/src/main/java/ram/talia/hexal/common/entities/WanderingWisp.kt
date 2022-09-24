package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.plus
import ram.talia.hexal.common.lib.HexalEntities
import kotlin.math.abs

class WanderingWisp	(entityType: EntityType<out WanderingWisp>, world: Level) : BaseWisp(entityType, world) {

	override var media: Int
		get() = MIN_MEDIA + (MAX_MEDIA - MIN_MEDIA) * tickCount / MAX_TICKS_ALIVE
		set(value) {}

	var acceleration: Vec3
		get() = Vec3(entityData.get(ACCELERATION_X).toDouble(), entityData.get(ACCELERATION_Y).toDouble(), entityData.get(ACCELERATION_Z).toDouble())
		set(value) {
			entityData.set(ACCELERATION_X, value.x.toFloat())
			entityData.set(ACCELERATION_Y, value.y.toFloat())
			entityData.set(ACCELERATION_Z, value.z.toFloat())
		}

	var startTick: Long = 0

	override fun maxSqrLinkRange() = 16.0

	override fun receiveIota(iota: SpellDatum<*>) { }

	override fun nextReceivedIota() = SpellDatum.make(Widget.NULL)

	override fun numRemainingIota() = 0

	override fun fightConsume(consumer: Either<BaseCastingWisp, ServerPlayer>) = false

	constructor(world: Level, pos: Vec3, media: Int) : this(HexalEntities.WANDERING_WISP, world) {
		setPos(pos)
		this.media = media
		startTick = world.gameTime
	}

	override fun tick() {
		super.tick()

		if (!level.isClientSide && level.gameTime > startTick + MAX_TICKS_ALIVE) {
			discard()
		}

		oldPos = position()

		move()

		if (level.isClientSide) {
			val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
			playWispParticles(colouriser)
			playTrailParticles(colouriser)
			playAllLinkParticles(colouriser)
		}
	}

	fun move() {
		val bBox = this.boundingBox
		val voxelShapes = level.getEntityCollisions(this, bBox.expandTowards(deltaMovement))
		val adjDelta = if (deltaMovement.lengthSqr() == 0.0) deltaMovement else collideBoundingBox(this, deltaMovement, bBox, level, voxelShapes)

		var dX = deltaMovement.x
		var dY = deltaMovement.y
		var dZ = deltaMovement.z
		var aX = acceleration.x
		var aY = acceleration.y
		var aZ = acceleration.z

		if (abs(adjDelta.x - dX) > 0.0001) {
			dX = -dX
			aX = -aX
		}
		if (abs(adjDelta.y - dY) > 0.0001) {
			dY = -dY
			aY = -aY
		}
		if (abs(adjDelta.z - dZ) > 0.0001) {
			dZ = -dZ
			aZ = -aZ
		}

		deltaMovement = Vec3(dX, dY, dZ)
		acceleration = Vec3(aX, aY, aZ)

		setPos(position() + adjDelta)

		deltaMovement += acceleration
		acceleration += Vec3(random.nextDouble(-0.005, 0.005), random.nextDouble(-0.005, 0.005), random.nextDouble(-0.005, 0.005))
		acceleration = Vec3(acceleration.x.coerceIn(-0.0125, 0.0125), acceleration.y.coerceIn(-0.0125, 0.0125), acceleration.z.coerceIn(-0.0125, 0.0125))
		deltaMovement = Vec3(deltaMovement.x.coerceIn(-0.05, 0.05), deltaMovement.y.coerceIn(-0.05, 0.05), deltaMovement.z.coerceIn(-0.05, 0.05))
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		entityData.set(ACCELERATION_X, compound.getFloat(TAG_ACCELERATION_X))
		entityData.set(ACCELERATION_Y, compound.getFloat(TAG_ACCELERATION_Y))
		entityData.set(ACCELERATION_Z, compound.getFloat(TAG_ACCELERATION_Z))
		startTick = compound.getLong(TAG_START_TICK)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		compound.putFloat(TAG_ACCELERATION_X, entityData.get(ACCELERATION_X))
		compound.putFloat(TAG_ACCELERATION_Y, entityData.get(ACCELERATION_Y))
		compound.putFloat(TAG_ACCELERATION_Z, entityData.get(ACCELERATION_Z))
		compound.putLong(TAG_START_TICK, startTick)
	}

	override fun defineSynchedData() {
		super.defineSynchedData()

		entityData.define(ACCELERATION_X, 0f)
		entityData.define(ACCELERATION_Y, 0f)
		entityData.define(ACCELERATION_Z, 0f)
	}

	companion object {
		@JvmStatic
		val ACCELERATION_X: EntityDataAccessor<Float> = SynchedEntityData.defineId(WanderingWisp::class.java, EntityDataSerializers.FLOAT)
		@JvmStatic
		val ACCELERATION_Y: EntityDataAccessor<Float> = SynchedEntityData.defineId(WanderingWisp::class.java, EntityDataSerializers.FLOAT)
		@JvmStatic
		val ACCELERATION_Z: EntityDataAccessor<Float> = SynchedEntityData.defineId(WanderingWisp::class.java, EntityDataSerializers.FLOAT)

		const val TAG_ACCELERATION_X = "acceleration_x"
		const val TAG_ACCELERATION_Y = "acceleration_y"
		const val TAG_ACCELERATION_Z = "acceleration_z"
		const val TAG_START_TICK = "start_tick"

		const val MAX_TICKS_ALIVE = 300
		const val MIN_MEDIA = 2 * ManaConstants.SHARD_UNIT
		const val MAX_MEDIA = 5 * ManaConstants.SHARD_UNIT
	}
}
