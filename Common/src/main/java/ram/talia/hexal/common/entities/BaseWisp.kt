package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.client.Minecraft
import net.minecraft.client.ParticleStatus
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nextColour
import kotlin.math.*

abstract class BaseWisp(entityType: EntityType<out BaseWisp>, world: Level)  : LinkableEntity(entityType, world), IMediaEntity<BaseWisp> {
	@Suppress("LeakingThis")
	var oldPos: Vec3 = position()

	override var media: Long
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, max(value, 0))

	override val isConsumable = true

	override fun get() = this

	override fun pigment(): FrozenPigment = FrozenPigment.fromNBT(entityData.get(PIGMENT))

	override fun getEyeHeight(pose: Pose, dim: EntityDimensions) = 0f

	override fun makeBoundingBox(): AABB {
		return super.makeBoundingBox().move(0.0, -getDimensions(Pose.STANDING).height*0.5, 0.0)
	}

	/**
	 * Set the look vector of the wisp equal a given vector
	 */
	fun setLookVector(vel: Vec3) {
		val direction = vel.normalize()

		val pitch = asin(direction.y)
		val yaw = asin(max(min(direction.x / cos(pitch), 1.0), -1.0))

		xRot = (-pitch * 180 / Math.PI).toFloat()
		yRot = (-yaw * 180 / Math.PI).toFloat()
		xRotO = xRot
		yRotO = yRot
	}

	/**
	 * Returns the maximum length vector that the wisp can move (up to the length of [step], and along the line of [step]) before it collides with something.
	 */
	fun maxMove(step: Vec3): Vec3 {
		val bBox = this.boundingBox
		val voxelShapes = level().getEntityCollisions(this, bBox.expandTowards(deltaMovement))
		return if (step.lengthSqr() == 0.0) step else collideBoundingBox(this, step, bBox, level(), voxelShapes)
	}

	fun renderCentre(): Vec3 = position()
	override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 = renderCentre()

	fun playTrailParticles() {
		playTrailParticles(pigment())
	}

	protected open fun playWispParticles(pigment: FrozenPigment) {
		val radius = (media.toDouble() / MediaConstants.DUST_UNIT).pow(1.0 / 3) / 100

		val repeats = when (Minecraft.getInstance().options.particles().get() as ParticleStatus) {
			ParticleStatus.ALL -> 50
			ParticleStatus.DECREASED -> 20
			ParticleStatus.MINIMAL -> 5
		}

		for (i in 0..repeats) {
			val colour: Int = pigment.nextColour(random)

			level().addParticle(
				ConjureParticleOptions(colour),
				(renderCentre().x + radius*random.nextGaussian()),
				(renderCentre().y + radius*random.nextGaussian()),
				(renderCentre().z + radius*random.nextGaussian()),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	protected open fun playTrailParticles(pigment: FrozenPigment) {
		val radius = ceil((media.toDouble() / MediaConstants.DUST_UNIT).pow(1.0 / 3) / 10)

		val delta = oldPos - position()
		val dist = delta.length() * 12 * radius * radius * radius

		for (i in 0..dist.toInt()) {
			val colour: Int = pigment.nextColour(random)

			val coeff = i / dist
			level().addParticle(
				ConjureParticleOptions(colour),
				(renderCentre().x + delta.x * coeff),
				(renderCentre().y + delta.y * coeff),
				(renderCentre().z + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	fun setPigment(pigment: FrozenPigment): FrozenPigment {
		entityData.set(PIGMENT, pigment.serializeToNBT())
		return pigment
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		entityData.set(PIGMENT, compound.getCompound(TAG_PIGMENT))

		media = compound.getLong(TAG_MEDIA)

		oldPos = position() // so that reloading a wisp doesn't result in it having a trail to the origin forever
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		compound.putCompound(TAG_PIGMENT, entityData.get(PIGMENT))
		compound.putLong(TAG_MEDIA, media)
	}

	override fun defineSynchedData() {
		entityData.define(PIGMENT, FrozenPigment.DEFAULT.get().serializeToNBT())
		entityData.define(MEDIA, 20L * MediaConstants.DUST_UNIT)
	}

	companion object {
		@JvmStatic
		val PIGMENT: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)

		@JvmStatic
		val MEDIA: EntityDataAccessor<Long> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.LONG)

		const val TAG_PIGMENT = "pigment"
		const val TAG_MEDIA = "media"
	}
}