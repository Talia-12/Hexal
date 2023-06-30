package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.EntityIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
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
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.linkable.ILinkable
import ram.talia.hexal.api.nbt.SerialisedIota
import ram.talia.hexal.api.nbt.SerialisedIotaList
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.spell.casting.triggers.IWispTrigger
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerRegistry
import ram.talia.hexal.client.sounds.WispCastingSoundInstance
import ram.talia.hexal.common.lib.HexalSounds
import ram.talia.hexal.common.network.MsgWispCastSoundAck
import ram.talia.hexal.xplat.IXplatAbstractions
import java.lang.Integer.min
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.pow


abstract class BaseCastingWisp(entityType: EntityType<out BaseCastingWisp>, world: Level) : BaseWisp(entityType, world) {
	open val shouldComplainNotEnoughMedia = true

	private var activeTrigger: IWispTrigger? = null
	private var soundInstance: WispCastingSoundInstance? = null

	var summonedChildThisCast = false

	private var casterUUID: UUID? = null

	var caster: Player? = null
		get() {
			return if (field != null && !field!!.isRemoved) {
				field
			} else if (casterUUID != null && level is ServerLevel) {
				field = (level as ServerLevel).getEntity(casterUUID!!) as? Player
				field
			} else {
				null
			}
		}
		set(value) {
			if (value != null) {
				casterUUID = value.uuid
				field = value
			}
		}

	override val isConsumable = true

	var seon: Boolean
		get() = entityData.get(SEON)
		set(value) = entityData.set(SEON, value)

	// true at the end will be not-ed to false by the ! out the front
	override fun fightConsume(consumer: Either<BaseCastingWisp, ServerPlayer>) = !(this.caster?.equals(consumer.map({ it.caster }, { it })) ?: false)

	val serHex: SerialisedIotaList = SerialisedIotaList()

	fun setHex(iotas: MutableList<Iota>) {
		serHex.set(iotas)

		hexNumTrueNames = 0
		for (entity in serHex.getReferencedEntities(level as ServerLevel)) {
			if ((entity is Player) && (entity!= caster)) {
				hexNumTrueNames++
			}
		}
	}

	private var scheduledCast: Boolean
		get() = entityData.get(SCHEDULED_CAST)
		set(value) = entityData.set(SCHEDULED_CAST, value)

	override fun get() = this

	constructor(entityType: EntityType<out BaseCastingWisp>, world: Level, pos: Vec3, caster: Player, media: Int) : this(entityType, world) {
		setPos(pos)
		this.caster = caster
		@Suppress("LeakingThis")
		this.media = media
	}


	override fun tick() {
		super.tick()

		// clear entities that have been removed from the world at least once per second
		// to prevent any memory leak type errors
		if (!level.isClientSide && (level.gameTime % 20 == 0L))
			serHex.refreshIotas(level as ServerLevel)

		// check if media is <= 0 ; destroy the wisp if it is, decrement the lifespan otherwise.
		if (media <= 0) {
			discard()
		}

//		HexalAPI.LOGGER.info("wisp $uuid ticked and ${if (scheduledCast) "does" else "doesn't"} have a cast scheduled.")

		oldPos = position()

		if (!scheduledCast && caster != null) {
			if (!level.isClientSide) {
				deductMedia()
				sendMediaToNeighbours()
			}

			childTick()
			move()
		}
		tryCheckInsideBlocks() // let the nether portal know if this wisp is inside it.

		// TODO: move all this into BaseWisp
		if (level.isClientSide) {
			val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
			playWispParticles(colouriser)
			playTrailParticles(colouriser)
			clientLinkableHolder!!.renderLinks()
		}
	}


	/**
	 * Called in [tick], expected to reduce the amount of [media] remaining in the wisp.
	 */
	fun deductMedia() {
		var cost = when(canScheduleCast()) {
			true  -> normalCostPerTick
			false -> untriggeredCostPerTick
		}
		cost += HexalConfig.server.linkUpkeepPerTick * numLinked()

		HexalAPI.LOGGER.debug("Num contained players: ${wispNumContainedPlayers()}")
		cost = (cost * HexalConfig.server.storingPlayerCostScaleFactor.pow(wispNumContainedPlayers().toDouble())).toInt()

		if (seon)
			cost = (cost / HexalConfig.server.seonDiscountFactor).toInt()
		media -= cost
	}

	//region Trueplayer handling stuff
	private var receivedIotasNumTrueNames: Int = 0
		set(value) { field = if (value >= 0) value else 0 }
	private var hexNumTrueNames: Int = 0
		set(value) { field = if (value >= 0) value else 0 }

	open fun wispNumContainedPlayers(): Int = receivedIotasNumTrueNames + hexNumTrueNames

	override fun owner(): UUID = casterUUID ?: uuid

	override fun receiveIota(sender: ILinkable, iota: Iota) {
		super.receiveIota(sender, iota)
		receivedIotasNumTrueNames += countTrueNamesInIota(iota, caster)
	}

	override fun nextReceivedIota(): Iota {
		val iota = super.nextReceivedIota()
		receivedIotasNumTrueNames -= countTrueNamesInIota(iota, caster)
		return iota
	}

	override fun clearReceivedIotas() {
		super.clearReceivedIotas()
		receivedIotasNumTrueNames = 0
	}

	fun countTrueNamesInIota(iota: Iota, caster: Player?): Int {
		val poolToSearch = ArrayDeque<Iota>()
		poolToSearch.addLast(iota)

		var numTrueNames = 0

		while (poolToSearch.isNotEmpty()) {
			val datumToCheck = poolToSearch.removeFirst()
			if (datumToCheck is EntityIota && datumToCheck.entity is Player && datumToCheck.entity != caster)
				numTrueNames += 1
			if (datumToCheck is ListIota)
				poolToSearch.addAll(datumToCheck.list)
		}

		return numTrueNames
	}
	//endregion

	open val normalCostPerTick: Int get() = HexalConfig.server.projectileWispUpkeepPerTick
	open val untriggeredCostPerTick: Int get() = (normalCostPerTick * HexalConfig.server.untriggeredWispUpkeepDiscount).toInt()

	private fun sendMediaToNeighbours() {
		for (i in 0 until this.numLinked()) {
			val linked = this.getLinked(i) ?: continue
			val requested = linked.canAcceptMedia(this, this.media)

			if (requested > 0) {
				val sent = min(requested, this.media)
				this.media -= sent
				linked.acceptMedia(this, sent)
			}
		}
	}

	override fun currentMediaLevel() = media

	override fun canAcceptMedia(other: ILinkable, otherMediaLevel: Int): Int {
		if (otherMediaLevel == -1)
			return (Int.MAX_VALUE - this.media)
		if (otherMediaLevel <= this.media)
			return 0

		return ((otherMediaLevel - this.media) * HexalConfig.server.mediaFlowRateOverLink).toInt()
	}

	override fun acceptMedia(other: ILinkable, sentMedia: Int) {
		media += sentMedia
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
			hex: SerialisedIotaList,
			initialStack: SerialisedIotaList,
			initialRavenmind: SerialisedIota,
	): Boolean {
		if (level.isClientSide || caster == null || !canScheduleCast())
			return false // return dummy data, not expecting anything to be done with it

		IXplatAbstractions.INSTANCE.getWispCastingManager(caster as ServerPlayer)
				.scheduleCast(this, priority, hex, initialStack, initialRavenmind)

//			HexalAPI.LOGGER.info("cast successfully scheduled, hex was $rHex, stack was $rInitialStack, ravenmind was $rInitialRavenmind")

		scheduledCast = true

		return scheduledCast
	}

	fun scheduleCastSound() {
		if (level.isClientSide)
			throw Exception("BaseWisp.scheduleCastSound should only be called on server.") // TODO
//		HexalAPI.LOGGER.debug("scheduling casting sound, level is $level")
		IXplatAbstractions.INSTANCE.sendPacketNear(position(), 32.0, level as ServerLevel, MsgWispCastSoundAck(this))
	}

	fun playCastSoundClient() {
		if (!level.isClientSide)
			throw Exception("BaseWisp.playCastSoundClient should only be called on client.") // TODO

//		HexalAPI.LOGGER.debug("playing casting sound, level is $level")
		if (soundInstance == null || soundInstance!!.isStopped) {
			soundInstance = WispCastingSoundInstance(this)
			Minecraft.getInstance().soundManager.play(soundInstance!!)
			HexalSounds.WISP_CASTING_START.playAt(level, position(), .3f, 1f + (random.nextFloat() - 0.5f) * 0.2f, false)
		}

		soundInstance!!.keepAlive()
	}

	open fun castCallback(result: WispCastingManager.WispCastResult) {
		scheduledCast = false

		if (result.cancelled)
			return

		// the cast errored, delete the wisp
		if (!result.succeeded)
			discard()

//		if (result.makesCastSound)
//			scheduleCastSound()
	}

	override fun setDeltaMovement(dV: Vec3) {
		super.setDeltaMovement(dV)
		setLookVector(dV)
	}

	override fun remove(reason: RemovalReason) {
		if (reason.shouldDestroy() && this.seon && this.caster != null)
			IXplatAbstractions.INSTANCE.setSeon(this.caster!! as ServerPlayer, null)
		super.remove(reason)
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		if (compound.hasUUID(TAG_CASTER)) {
			casterUUID = compound.getUUID(TAG_CASTER)
//			HexalAPI.LOGGER.info("loading wisp $uuid's casterUUID as $casterUUID")
		}

		when (val hexTag = compound.get(TAG_HEX)) {
			null -> serHex.set(mutableListOf())
			else -> serHex.set(hexTag as ListTag)
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
		compound.put(TAG_HEX, serHex.getTag())
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
		super.getAddEntityPacket() // called to call LinkableEntity.linkableHolder.syncAll()
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
		const val TAG_ACTIVE_TRIGGER = "active_trigger"
		const val TAG_SEON = "seon"
	}
}

