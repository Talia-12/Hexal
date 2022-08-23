package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.*
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.spell.toIotaList
import ram.talia.hexal.api.spell.toNbtList
import ram.talia.hexal.api.times
import ram.talia.hexal.xplat.IXplatAbstractions
import kotlin.math.ceil
import kotlin.math.pow


abstract class BaseWisp : Projectile {
	open val shouldComplainNotEnoughMedia = true

	var media: Int
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, value)

	var hex: List<SpellDatum<*>> = ArrayList()

	private var scheduledCast: Boolean
		get() = entityData.get(SCHEDULED_CAST)
		set(value) = entityData.set(SCHEDULED_CAST, value)

	private var oldPos: Vec3 = position()

	var velocity: Vec3
		get() = scaleVecByMedia(deltaMovement)
		set(value) { deltaMovement = value }

	fun addMedia(dMedia: Int) {
		media += dMedia
	}

	// error here isn't actually a problem
	//TODO: if the owner is null on the server we need to do SOMETHING to handle it
	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world) {
//		HexalAPI.LOGGER.info("constructor for $uuid called!")
//		lastTick = world.gameTime - 1
	}

	constructor(entityType: EntityType<out BaseWisp>, world: Level, pos: Vec3, caster: Player, media: Int) : super(entityType, world) {
//		HexalAPI.LOGGER.info("constructor for $uuid called!")
		setPos(pos)
		owner = caster
		this.media = media
//		lastTick = world.gameTime - 1
	}

	override fun tick() {
		super.tick()

		// make sure tick isn't called twice, since tick() is also called by castCallback to ensure wisps that need ticking don't actually get skipped on the tick that their
		// cast is successful. Not actually doing anything right now since tick() isn't currently being called twice.
//		if (lastTick == level.gameTime)
//			return
//		lastTick = level.gameTime

		// check if media is <= 0 ; destroy the wisp if it is, decrement the lifespan otherwise.
		if (media <= 0) {
			discard()
		}

		if (!scheduledCast) {
			if (!level.isClientSide)
				deductMedia()

			oldPos = position()

			childTick()
			move()
		}

		if (level.isClientSide) {
			val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
			playParticles(colouriser)
		}
	}
	

	/**
	 * Called in [tick], expected to reduce the amount of [media] remaining in the wisp.
	 */
	open fun deductMedia() {
		media -= WISP_COST_PER_TICK
	}


	/**
	 * Called in [tick] to execute other code that child classes may want to execute every tick; respects not executing if the wisp is waiting for a cast to be executed.
	 * Is called before [move]
	 */
	open fun childTick() {}

	/**
	 * Called in [tick], expected to update the Wisp's position. Is called after [childTick].
	 */
	abstract fun move()

	/**
	 * Called in [ram.talia.hexal.mixin.MixinCastingContext.isVecInRangeWisp] to determine the
	 * maximum range the wisp should be able to affect and make them able to affect things inside that range.
	 */
	abstract fun maxSqrCastingDistance(): Double

	/**
	 * Schedules casting the hex passed as [hex], with the initial stack [initialStack] and initial ravenmind [initialRavenmind]. If a callback is needed (e.g. to save
	 * the results of the cast somewhere) a callback can be provided as [castCallback]. Returns whether the hex was successfully scheduled.
	 */
	fun scheduleCast(
		priority: Int,
		hex: List<SpellDatum<*>>,
		initialStack: MutableList<SpellDatum<*>> = ArrayList<SpellDatum<*>>().toMutableList(),
		initialRavenmind: SpellDatum<*> = SpellDatum.make(Widget.NULL),
	): Boolean {
		if (level.isClientSide || owner == null)
			return false // return dummy data, not expecting anything to be done with it

		val sPlayer = owner as ServerPlayer

		IXplatAbstractions.INSTANCE.getWispCastingManager(sPlayer).scheduleCast(this, priority, hex, initialStack, initialRavenmind)

		scheduledCast = true

		return true
	}

	open fun castCallback(result: WispCastingManager.WispCastResult) {
		scheduledCast = false
		// turned off since it's causing PROBLEMS. TODO: Figure out how to actually do this properly.
//		processTick()
	}

	fun addVelocity(vel: Vec3) {
		deltaMovement += scaleVecByMedia(vel)
	}

	private fun scaleVecByMedia(vec: Vec3) = scaleVecByMedia(vec, 1, media)

	private fun scaleVecByMedia(vec: Vec3, oldMedia: Int, newMedia: Int): Vec3 {
		val WIDTH_SCALE = 0.015
		val LIMIT = 0.25

		val oldScale = (1 - LIMIT) / ((oldMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) * (oldMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) + 1) + LIMIT
		val newScale = (1 - LIMIT) / ((newMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) * (newMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) + 1) + LIMIT
		return (newScale / oldScale) * vec
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
			boundingBox.expandTowards(velocity).inflate(1.0),
			this::canHitEntity
		)

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

	override fun onHitEntity(result: EntityHitResult) {
		super.onHitEntity(result)
	}

	override fun onHitBlock(result: BlockHitResult) {
		super.onHitBlock(result)
	}

	fun playParticles() {
		val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
		playParticles(colouriser)
	}
	open protected fun playParticles(colouriser: FrozenColorizer) {
		val radius = ceil((media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 10)

		val delta = position() - oldPos
		val dist = delta.length() * 12 * radius*radius*radius

		for (i in 0..dist.toInt()) {
			val colour: Int = colouriser.nextColour()

			val coeff = i / dist
			level.addParticle(
				ConjureParticleOptions(colour, false),
				(oldPos.x + delta.x * coeff),
				(oldPos.y + delta.y * coeff),
				(oldPos.z + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}

		// this doesn't actually look very good
//		for (i in 0..(4*radius*radius*radius).toInt()) {
//			val colour: Int = colouriser.nextColour()
//
//			level.addParticle(
//				ConjureParticleOptions(colour, true),
//				(position().x + radius*random.nextGaussian()),
//				(position().y + radius*random.nextGaussian()),
//				(position().z + radius*random.nextGaussian()),
//				0.0125 * (random.nextDouble() - 0.5),
//				0.0125 * (random.nextDouble() - 0.5),
//				0.0125 * (random.nextDouble() - 0.5)
//			)
//		}
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

	override fun load(compound: CompoundTag) {
		// assuming this is for saving/loading chunks and the game
		super.load(compound)
		entityData.set(COLOURISER, compound.getCompound(TAG_COLOURISER))
		if (!level.isClientSide) {
			hex = compound.getList(TAG_HEX, compound.getInt(TAG_HEX_LENGTH)).toIotaList(level as ServerLevel)
		}
		media = compound.getInt(TAG_MEDIA)
		scheduledCast = compound.getBoolean(TAG_SCHEDULED_CAST)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)
		compound.putCompound(TAG_COLOURISER, entityData.get(COLOURISER))
		if (!level.isClientSide) {
			compound.putInt(TAG_HEX_LENGTH, hex.size)
			compound.putList(TAG_HEX, hex.toNbtList())
		}
		compound.putInt(TAG_MEDIA, media)
		compound.putBoolean(TAG_SCHEDULED_CAST, scheduledCast)
	}

	override fun defineSynchedData() {
		// defines the entry in SynchedEntityData associated with the EntityDataAccessor COLOURISER, and gives it a default value
//		HexalAPI.LOGGER.info("defineSynchedData for $uuid called!")
		entityData.define(COLOURISER, FrozenColorizer.DEFAULT.get().serializeToNBT())
		entityData.define(MEDIA, 20*ManaConstants.DUST_UNIT)
		entityData.define(SCHEDULED_CAST, false)
	}

	companion object {
		@JvmField
		val COLOURISER: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)
		val MEDIA: EntityDataAccessor<Int> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.INT)
		val SCHEDULED_CAST: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.BOOLEAN)

		const val TAG_COLOURISER = "colouriser"
		const val TAG_HEX_LENGTH = "hex_length"
		const val TAG_HEX = "hex"
		const val TAG_MEDIA = "media"
		const val TAG_SCHEDULED_CAST = "scheduled_cast"

		const val WISP_COST_PER_TICK = (ManaConstants.SHARD_UNIT / 20.0).toInt()
	}
}

