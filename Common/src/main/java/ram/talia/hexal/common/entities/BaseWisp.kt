package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.*
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface


abstract class BaseWisp : Projectile {
	var media = WISP_COST_PER_TICK * ManaConstants.DUST_UNIT

	private var oldPos: Vec3 = position()

	fun addMedia(dMedia: Int) {
		media += dMedia
	}

	// error here isn't actually a problem
	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)

	constructor(world: Level, pos: Vec3, caster: Player, media: Int) : super(HexalEntities.PROJECTILE_WISP, world) {
		setPos(pos)
		owner = caster
		this.media = media
	}

	override fun tick() {
		super.tick()

		HexalAPI.LOGGER.info("media: $media")
		HexalAPI.LOGGER.info("cost: $WISP_COST_PER_TICK")

		// check if lifespan is < 0 ; destroy the wisp if it is, decrement the lifespan otherwise.
		if (media <= 0) discard()
		media -= WISP_COST_PER_TICK

		oldPos = position()

		move()

		if(level.isClientSide) {
			playParticles();
		}
	}

	/**
	 * Called in [tick], expected to update the Wisp's position.
	 */
	abstract fun move()

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
	}

	override fun onHitBlock(result: BlockHitResult) {
		super.onHitBlock(result)
	}

	/**
	 * Casts the spell passed as [hex], with the initial stack [initialStack] and initial ravenmind [initialRavenmind], and returns a pair containing the final state
	 * of the stack and ravenmind.
	 */
	fun castSpell(
		hex: List<SpellDatum<*>>,
		initialStack: MutableList<SpellDatum<*>> = ArrayList<SpellDatum<*>>().toMutableList(),
		initialRavenmind: SpellDatum<*> = SpellDatum.make(Widget.NULL)
	): Pair<MutableList<SpellDatum<*>>, SpellDatum<*>> {
		if (level.isClientSide)
			return Pair(initialStack, initialRavenmind) // return dummy data, not expecting anything to be done with it

		HexalAPI.LOGGER.info(position())

		val sPlayer = owner as ServerPlayer
		val ctx = CastingContext(
			sPlayer,
			InteractionHand.MAIN_HAND
		)

		// IntelliJ is complaining that ctx will never be an instance of MixinCastingContextInterface cause it doesn't know about mixin, but we know better
		val mCast = ctx as? MixinCastingContextInterface
		mCast?.wisp = this

		val harness = CastingHarness(ctx)

		harness.stack = initialStack
		harness.localIota = initialRavenmind

		val info = harness.executeIotas(hex, sPlayer.getLevel())

		if (info.makesCastSound) {
			sPlayer.level.playSound(
				null, position().x, position().y, position().z,
				HexSounds.ACTUALLY_CAST, SoundSource.PLAYERS, 1f,
				1f + (Math.random().toFloat() - 0.5f) * 0.2f
			)
		}

		return Pair(harness.stack, harness.localIota)
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

		const val MAX_DISTANCE_TO_WISP = 2.5
		const val WISP_COST_PER_TICK = (3.0/20.0 * ManaConstants.DUST_UNIT).toInt()
	}
}

