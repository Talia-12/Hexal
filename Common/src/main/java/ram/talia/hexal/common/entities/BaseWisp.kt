package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus
import kotlin.math.*

abstract class BaseWisp(entityType: EntityType<out BaseWisp>, world: Level)  : LinkableEntity(entityType, world), IMediaEntity<BaseWisp> {
	var oldPos: Vec3 = position()

	override var media: Int
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, max(value, 0))

	override val isConsumable = true

	override fun get() = this

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

	fun playTrailParticles() {
		val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
		playTrailParticles(colouriser)
	}

	override fun renderCentre(): Vec3 = position()

	protected open fun playWispParticles(colouriser: FrozenColorizer) {
		val radius = (media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 100

		for (i in 0..50) {
			val colour: Int = colouriser.nextColour()

			level.addParticle(
				ConjureParticleOptions(colour, true),
				(renderCentre().x + radius*random.nextGaussian()),
				(renderCentre().y + radius*random.nextGaussian()),
				(renderCentre().z + radius*random.nextGaussian()),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	protected open fun playTrailParticles(colouriser: FrozenColorizer) {
		val radius = ceil((media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 10)

		val delta = oldPos - position()
		val dist = delta.length() * 12 * radius * radius * radius

		for (i in 0..dist.toInt()) {
			val colour: Int = colouriser.nextColour()

			val coeff = i / dist
			level.addParticle(
				ConjureParticleOptions(colour, false),
				(renderCentre().x + delta.x * coeff),
				(renderCentre().y + delta.y * coeff),
				(renderCentre().z + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	fun playLinkParticles(colouriser: FrozenColorizer) {
		for (renderLink in renderLinks) {
			val delta = renderLink.renderCentre() - renderCentre()
			val dist = delta.length() * 12

			for (i in 0..dist.toInt()) {
				val colour: Int = colouriser.nextColour()

				val coeff = i / dist
				level.addParticle(
					ConjureParticleOptions(colour, false),
					(renderCentre().x + delta.x * coeff),
					(renderCentre().y + delta.y * coeff),
					(renderCentre().z + delta.z * coeff),
					0.0125 * (random.nextDouble() - 0.5),
					0.0125 * (random.nextDouble() - 0.5),
					0.0125 * (random.nextDouble() - 0.5)
				)
			}
		}
	}

	fun FrozenColorizer.nextColour(): Int {
		return getColor(
			random.nextFloat() * 16384,
			Vec3(
				random.nextFloat().toDouble(),
				random.nextFloat().toDouble(),
				random.nextFloat().toDouble()
			).scale((random.nextFloat() * 3).toDouble())
		)
	}

	fun setColouriser(colouriser: FrozenColorizer) {
		entityData.set(COLOURISER, colouriser.serializeToNBT())
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		entityData.set(COLOURISER, compound.getCompound(TAG_COLOURISER))

		media = compound.getInt(TAG_MEDIA)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		compound.putCompound(TAG_COLOURISER, entityData.get(COLOURISER))
		compound.putInt(TAG_MEDIA, media)
	}

	override fun defineSynchedData() {
		super.defineSynchedData()

		entityData.define(COLOURISER, FrozenColorizer.DEFAULT.get().serializeToNBT())
		entityData.define(MEDIA, 20 * ManaConstants.DUST_UNIT)
	}

	override fun getAddEntityPacket(): Packet<*> {
		return ClientboundAddEntityPacket(this, 0)
	}

	companion object {
		@JvmStatic
		val COLOURISER: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)

		@JvmStatic
		val MEDIA: EntityDataAccessor<Int> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.INT)

		const val TAG_COLOURISER = "colouriser"
		const val TAG_MEDIA = "media"
	}
}