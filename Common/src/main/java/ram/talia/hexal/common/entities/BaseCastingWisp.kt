package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.utils.asCompound
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.*
import ram.talia.hexal.api.nbt.LazyIotaList
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.*
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.spell.casting.triggers.IWispTrigger
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerRegistry
import ram.talia.hexal.api.times
import ram.talia.hexal.xplat.IXplatAbstractions
import java.util.*
import kotlin.math.*


abstract class BaseCastingWisp(entityType: EntityType<out BaseCastingWisp>, world: Level) : BaseWisp(entityType, world) {
	open val shouldComplainNotEnoughMedia = true

	private var activeTrigger: IWispTrigger? = null

	private var casterUUID: UUID? = null
	private var cachedCaster: Player? = null

	var caster: Player?
		get() {
			return if (cachedCaster != null && !cachedCaster!!.isRemoved) {
				cachedCaster
			} else if (casterUUID != null && level is ServerLevel) {
				cachedCaster = (level as ServerLevel).getEntity(casterUUID!!) as? Player
				cachedCaster
			} else {
				null
			}
		}
		set(value) {
			if (value != null) {
				casterUUID = value.uuid
				cachedCaster = value
			}
		}

	override var media: Int
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, max(value, 0))

	override val isConsumable = true

	// true at the end will be not-ed to false by the ! out the front
	override fun fightConsume(consumer: Either<BaseCastingWisp, ServerPlayer>) = !(this.caster?.equals(consumer.map({ it.caster }, { it })) ?: false)

	// LazyIotaList used so that the ListTag loaded from NBT is only converted
	// into a List of SpellDatum's when needed, meaning that it's guaranteed
	// to happen at the point where Level.getEntity works properly.
	var hex: List<SpellDatum<*>>
		get() {
			if (level.isClientSide)
				throw Exception("BaseCastingWisp.hex should only be accessed on server.") // TODO: create and replace with ServerOnlyException
			return lazyHex!!.get()
		}
		set(value) {
			lazyHex?.set(value as MutableList<SpellDatum<*>>)
		}
	private val lazyHex: LazyIotaList? = if (level.isClientSide) null else LazyIotaList(level as ServerLevel)

	var receivedIotas: MutableList<SpellDatum<*>>
		get() {
			if (level.isClientSide)
				throw Exception("BaseCastingWisp.receivedIotas should only be accessed on server.") // TODO: create and replace with ServerOnlyException
			return lazyReceivedIotas!!.get()
		}
		set(value) {
			lazyReceivedIotas?.set(value)
		}
	private val lazyReceivedIotas: LazyIotaList? = if (level.isClientSide) null else LazyIotaList(level as ServerLevel)

	private var scheduledCast: Boolean
		get() = entityData.get(SCHEDULED_CAST)
		set(value) = entityData.set(SCHEDULED_CAST, value)

	var velocity: Vec3
		get() = scaleVecByMedia(deltaMovement)
		set(value) {
			// change the wisp to look where its velocity points, useful for blinking
			setLookVector(value)
			deltaMovement = value
		}

	override fun get() = this

	constructor(entityType: EntityType<out BaseCastingWisp>, world: Level, pos: Vec3, caster: Player, media: Int) : this(entityType, world) {
		setPos(pos)
		this.caster = caster
		this.media = media
	}

	open fun getEffectSource(): Entity {
		return this.caster ?: this
	}

	override fun getEyeHeight(pose: Pose, dim: EntityDimensions): Float {
		return dim.height * 0.5f
	}


	override fun tick() {
		super.tick()

		// check if media is <= 0 ; destroy the wisp if it is, decrement the lifespan otherwise.
		if (media <= 0) {
			discard()
		}

//		HexalAPI.LOGGER.info("wisp $uuid ticked and ${if (scheduledCast) "does" else "doesn't"} have a cast scheduled.")

		if (!scheduledCast || caster == null) {
			if (!level.isClientSide)
				deductMedia()

			oldPos = position()

			childTick()
			move()
		}

		if (level.isClientSide) {
			val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
			playWispParticles(colouriser)
			playTrailParticles(colouriser)
			playLinkParticles(colouriser)
		}
	}


	/**
	 * Called in [tick], expected to reduce the amount of [media] remaining in the wisp.
	 */
	open fun deductMedia() {
		media -= when(canScheduleCast()) {
			true  -> WISP_COST_PER_TICK_NORMAL
			false -> WISP_COST_PER_TICK_UNTRIGGERED
		}
		media -= COST_PER_LINK_PER_TICK * linked.size
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

	fun setTrigger(trigger: IWispTrigger) {
		activeTrigger = trigger
	}

	/**
	 * Returns true if there are no triggers limiting when the wisp can cast, false otherwise
	 */
	fun canScheduleCast(): Boolean {
//		HexalAPI.LOGGER.info("active trigger is $activeTrigger, shouldRemove: ${activeTrigger?.shouldRemoveTrigger(this)}, shouldTrigger: ${activeTrigger?.shouldTrigger(this)}")
		if (activeTrigger?.shouldRemoveTrigger(this) == true)
			activeTrigger = null
		return activeTrigger?.shouldTrigger(this) ?: true
	}

	/**
	 * Schedules casting the hex passed as [hex], with the initial stack [initialStack] and initial ravenmind [initialRavenmind]. If a callback is needed (e.g. to save
	 * the results of the cast somewhere) a callback can be provided as [castCallback]. Returns whether the hex was successfully scheduled.
	 */
	fun scheduleCast(
		priority: Int,
		hex: List<SpellDatum<*>>,
		initialStack: MutableList<SpellDatum<*>> = ArrayList<SpellDatum<*>>(),
		initialRavenmind: SpellDatum<*> = SpellDatum.make(Widget.NULL),
	): Boolean {
		val asdf = !canScheduleCast()
//		HexalAPI.LOGGER.info("will skip schedule if any of ${level.isClientSide}, ${caster == null}, $asdf are true.")
		if (level.isClientSide || caster == null || asdf)
			return false // return dummy data, not expecting anything to be done with it

		val sPlayer = caster as ServerPlayer

		IXplatAbstractions.INSTANCE.getWispCastingManager(sPlayer).ifPresent {
			it.scheduleCast(this, priority, hex, initialStack, initialRavenmind)

//			HexalAPI.LOGGER.info("cast successfully scheduled, hex was $rHex, stack was $rInitialStack, ravenmind was $rInitialRavenmind")

			scheduledCast = true
		}

		return scheduledCast
	}

	override fun receiveIota(iota: SpellDatum<*>) {
		receivedIotas.add(iota)
	}

	override fun nextReceivedIota(): SpellDatum<*> {
		if (receivedIotas.size == 0) {
			return SpellDatum.make(Widget.NULL)
		}

		val iota = receivedIotas[0]
		receivedIotas.removeAt(0)

		return iota
	}

	override fun numRemainingIota(): Int {
		return receivedIotas.size
	}

	open fun castCallback(result: WispCastingManager.WispCastResult) {
		// the cast errored, delete the wisp
		if (!result.succeeded)
			discard()

		if (result.makesCastSound)
			playCastSound()

		scheduledCast = false
	}

	fun addVelocityScaled(vel: Vec3) {
		deltaMovement += scaleVecByMedia(vel)
		// change the wisp to look where its velocity points, useful for blinking
		setLookVector(deltaMovement)
	}

	override fun push(x: Double, y: Double, z: Double) {
		// TODO: figure out how I actually want this to work since it is desireable for OpAddMove (or whatever) and OpGetMove to return what you'd expect
		// TODO: but also desireable for it to be setup such that your ballistics maths doesn't need to account for the velocity scaling stuff at all
		deltaMovement += Vec3(x, y, z)
		// change the wisp to look where its velocity points, useful for blinking
		setLookVector(deltaMovement)
		hasImpulse = true
	}

	private fun scaleVecByMedia(vec: Vec3) = scaleVecByMedia(vec, 1, media)

	private fun scaleVecByMedia(vec: Vec3, oldMedia: Int, newMedia: Int): Vec3 {
		val WIDTH_SCALE = 0.015
		val LIMIT = 0.25

		val oldScale = (1 - LIMIT) / ((oldMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) * (oldMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) + 1) + LIMIT
		val newScale = (1 - LIMIT) / ((newMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) * (newMedia * WIDTH_SCALE / ManaConstants.DUST_UNIT) + 1) + LIMIT
		return (newScale / oldScale) * vec
	}


	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		if (compound.hasUUID(TAG_CASTER)) {
			casterUUID = compound.getUUID(TAG_CASTER)
//			HexalAPI.LOGGER.info("loading wisp $uuid's casterUUID as $casterUUID")
		}

		when (val hexTag = compound.get(TAG_HEX)) {
			null -> lazyHex!!.set(mutableListOf())
			else -> lazyHex!!.set(hexTag as ListTag)
		}

//		HexalAPI.LOGGER.info("loading wisp $uuid's hex from $hexTag")

		when (val receivedIotasTag = compound.get(TAG_RECEIVED_IOTAS)) {
			null -> lazyReceivedIotas!!.set(mutableListOf())
			else -> lazyReceivedIotas!!.set(receivedIotasTag as ListTag)
		}

		activeTrigger = when (val activeTriggerTag = compound.get(TAG_ACTIVE_TRIGGER)) {
			null -> null
			else -> WispTriggerRegistry.fromNbt(activeTriggerTag.asCompound, level as ServerLevel)
		}
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

//		HexalAPI.LOGGER.info("saving wisp $uuid's caster $caster based on $casterUUID")

		if (casterUUID != null)
			compound.putUUID(TAG_CASTER, casterUUID!!)

//		HexalAPI.LOGGER.info("saving wisp $uuid's hex as $hexTag")
		compound.put(TAG_HEX, lazyHex!!.getUnloaded())
		compound.put(TAG_RECEIVED_IOTAS, lazyReceivedIotas!!.getUnloaded())
		if (activeTrigger != null)
			compound.put(TAG_ACTIVE_TRIGGER, WispTriggerRegistry.wrapNbt(activeTrigger!!))
	}

	override fun defineSynchedData() {
		super.defineSynchedData()

		// defines the entry in SynchedEntityData associated with the EntityDataAccessor COLOURISER, and gives it a default value
//		HexalAPI.LOGGER.info("defineSynchedData for $uuid called!")
		entityData.define(SCHEDULED_CAST, false)
	}

	override fun getAddEntityPacket(): Packet<*> {
		return ClientboundAddEntityPacket(this, caster?.id ?: 0)
	}

	override fun recreateFromPacket(packet: ClientboundAddEntityPacket) {
		super.recreateFromPacket(packet)
		val caster = level.getEntity(packet.data) as? Player
		if (caster != null) {
			this.caster = caster
		}
	}

	companion object {
		@JvmStatic
		val SCHEDULED_CAST: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(BaseCastingWisp::class.java, EntityDataSerializers.BOOLEAN)

		const val TAG_CASTER = "caster"
		const val TAG_HEX = "hex"
		const val TAG_RECEIVED_IOTAS = "received_iotas"
		const val TAG_ACTIVE_TRIGGER = "active_trigger"

		const val WISP_COST_PER_TICK_NORMAL      = (0.325 * ManaConstants.DUST_UNIT / 20.0).toInt()
		const val WISP_COST_PER_TICK_UNTRIGGERED = (0.25  * ManaConstants.DUST_UNIT / 20.0).toInt()
		const val COST_PER_LINK_PER_TICK = (0.01 * ManaConstants.DUST_UNIT / 20.0).toInt()
	}
}

