package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.hasByte
import com.mojang.datafixers.util.Either
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.*
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.nbt.LazyIotaList
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.spell.casting.triggers.IWispTrigger
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerRegistry
import ram.talia.hexal.client.sounds.WispCastingSoundInstance
import ram.talia.hexal.common.lib.HexalSounds
import ram.talia.hexal.common.network.MsgWispCastSoundAck
import ram.talia.hexal.xplat.IXplatAbstractions
import java.util.*
import kotlin.math.*


abstract class BaseCastingWisp(entityType: EntityType<out BaseCastingWisp>, world: Level) : BaseWisp(entityType, world) {
	open val shouldComplainNotEnoughMedia = true

	private var activeTrigger: IWispTrigger? = null
	private var soundInstance: WispCastingSoundInstance? = null

	var summonedChildThisCast = false

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

	var seon: Boolean
		get() = entityData.get(SEON)
		set(value) = entityData.set(SEON, value)

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
			(value as? MutableList<SpellDatum<*>>)?.let { lazyHex?.set(it) }
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

	override fun get() = this

	constructor(entityType: EntityType<out BaseCastingWisp>, world: Level, pos: Vec3, caster: Player, media: Int) : this(entityType, world) {
		setPos(pos)
		this.caster = caster
		this.media = media
	}


	override fun tick() {
		super.tick()

		// check if media is <= 0 ; destroy the wisp if it is, decrement the lifespan otherwise.
		if (media <= 0) {
			discard()
		}

//		HexalAPI.LOGGER.info("wisp $uuid ticked and ${if (scheduledCast) "does" else "doesn't"} have a cast scheduled.")

		oldPos = position()

		if (!scheduledCast && caster != null) {
			if (!level.isClientSide)
				deductMedia()

			childTick()
			move()
		}

		if (level.isClientSide) {
			val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
			playWispParticles(colouriser)
			playTrailParticles(colouriser)
			playAllLinkParticles()
		}
	}


	/**
	 * Called in [tick], expected to reduce the amount of [media] remaining in the wisp.
	 */
	fun deductMedia() {
		var cost = when(canScheduleCast()) {
			true  -> normalCostPerTick
			false -> WISP_COST_PER_TICK_UNTRIGGERED
		}
		cost += COST_PER_LINK_PER_TICK * linked.size
		if (seon)
			cost /= SEON_DISCOUNT_FACTOR
		media -= cost
	}

	open val normalCostPerTick = WISP_COST_PER_TICK_NORMAL


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

	override fun maxSqrLinkRange() = maxSqrCastingDistance()

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
		if (level.isClientSide || caster == null || !canScheduleCast())
			return false // return dummy data, not expecting anything to be done with it

		val sPlayer = caster as ServerPlayer

		IXplatAbstractions.INSTANCE.getWispCastingManager(sPlayer).scheduleCast(this, priority, hex, initialStack, initialRavenmind)

//			HexalAPI.LOGGER.info("cast successfully scheduled, hex was $rHex, stack was $rInitialStack, ravenmind was $rInitialRavenmind")

		scheduledCast = true

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

	fun scheduleCastSound() {
		if (level.isClientSide)
			throw Exception("BaseWisp.scheduleCastSound should only be called on server.") // TODO: create and replace with ServerOnlyException
		HexalAPI.LOGGER.info("scheduling casting sound, level is $level")
		IXplatAbstractions.INSTANCE.sendPacketNear(position(), 32.0, level as ServerLevel, MsgWispCastSoundAck(this))
	}

	fun playCastSoundClient() {
		if (!level.isClientSide)
			throw Exception("BaseWisp.playCastSoundClient should only be called on client.") // TODO: create and replace with ClientOnlyException

		HexalAPI.LOGGER.info("playing casting sound, level is $level")
		if (soundInstance == null || soundInstance!!.isStopped) {
			soundInstance = WispCastingSoundInstance(this)
			Minecraft.getInstance().soundManager.play(soundInstance!!)
			HexalSounds.WISP_CASTING_START.playAt(level, position(), .3f, 1f + (random.nextFloat() - 0.5f) * 0.2f, false)
		}

		soundInstance!!.keepAlive()
	}

	open fun castCallback(result: WispCastingManager.WispCastResult) {
		// the cast errored, delete the wisp
		if (!result.succeeded)
			discard()

		if (result.makesCastSound)
			scheduleCastSound()

		scheduledCast = false
	}

	override fun setDeltaMovement(dV: Vec3) {
		super.setDeltaMovement(dV)
		setLookVector(dV)
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

		seon = if (compound.hasByte(TAG_SEON)) { compound.getBoolean(TAG_SEON) } else { false }
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
		compound.putBoolean(TAG_SEON, seon)
	}

	override fun defineSynchedData() {
		super.defineSynchedData()

		// defines the entry in SynchedEntityData associated with the EntityDataAccessor COLOURISER, and gives it a default value
//		HexalAPI.LOGGER.info("defineSynchedData for $uuid called!")
		entityData.define(SCHEDULED_CAST, false)
		entityData.define(SEON, false)
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
		@JvmStatic
		val SEON: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(BaseCastingWisp::class.java, EntityDataSerializers.BOOLEAN)


		const val TAG_CASTER = "caster"
		const val TAG_HEX = "hex"
		const val TAG_RECEIVED_IOTAS = "received_iotas"
		const val TAG_ACTIVE_TRIGGER = "active_trigger"
		const val TAG_SEON = "seon"

		const val WISP_COST_PER_TICK_NORMAL      = (0.325 * ManaConstants.DUST_UNIT / 20.0).toInt()
		const val WISP_COST_PER_TICK_UNTRIGGERED = (0.25  * ManaConstants.DUST_UNIT / 20.0).toInt()
		const val COST_PER_LINK_PER_TICK = (0.01 * ManaConstants.DUST_UNIT / 20.0).toInt()
		const val SEON_DISCOUNT_FACTOR = 20
	}
}

