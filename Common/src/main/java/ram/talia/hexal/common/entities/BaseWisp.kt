package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.utils.asList
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import com.google.common.base.MoreObjects
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
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.*
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.*
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.times
import ram.talia.hexal.xplat.IXplatAbstractions
import java.util.*
import kotlin.math.*


abstract class BaseWisp : LinkableEntity {
	open val shouldComplainNotEnoughMedia = true

	private var casterUUID: UUID? = null
	private var cachedCaster: Entity? = null

	var caster: Entity?
		get() {
			return if (cachedCaster != null && !cachedCaster!!.isRemoved) {
				cachedCaster
			} else if (casterUUID != null && level is ServerLevel) {
				cachedCaster = (level as ServerLevel).getEntity(casterUUID!!)
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

	var media: Int
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, max(value, 0))

	// Either used so that loading from NBT results in lazy loading where the ListTag
	// is only converted into a List of SpellDatum's when needed, meaning that it's
	// guaranteed to happen at the point where Level.getEntity works properly.
	var hex: Either<List<SpellDatum<*>>, ListTag> = Either.left(ArrayList())

	private var linked: Either<MutableList<LinkableEntity>, ListTag> = Either.left(ArrayList())
	var renderLinks: MutableList<LinkableEntity>
		get() {
			if (level.isClientSide)
				return entityData.get(RENDER_LINKS).get(TAG_RENDER_LINKS)!!.asList.toIntList().mapNotNull { level.getEntity(it) as LinkableEntity } as MutableList<LinkableEntity>
			return renderLinksList.map({ it }, { it.toEntityList(level as ServerLevel) })
		}
		set(value) {
			if (level.isClientSide)
				return

			renderLinksList = Either.left(value)

			syncRenderLinks()
		}

	private var renderLinksList: Either<MutableList<LinkableEntity>, ListTag> = Either.left(ArrayList())

	private fun syncRenderLinks() {
		val compound = CompoundTag()
		val rRenderLinksList = renderLinksList.map({ it }, { it.toEntityList(level as ServerLevel) })
		compound.put(TAG_RENDER_LINKS, rRenderLinksList.map { it.id }.toNbtList())
		entityData.set(RENDER_LINKS, compound)
	}

	fun addRenderLink(other: LinkableEntity) {
		renderLinks.add(other)
		syncRenderLinks()
	}

	fun removeRenderLink(other: LinkableEntity) {
		renderLinks.remove(other)
		syncRenderLinks()
	}

	fun removeRenderLink(index: Int) {
		renderLinks.removeAt(index)
		syncRenderLinks()
	}

	var receivedIotas: Either<MutableList<SpellDatum<*>>, ListTag> = Either.left(ArrayList())

	private var scheduledCast: Boolean
		get() = entityData.get(SCHEDULED_CAST)
		set(value) = entityData.set(SCHEDULED_CAST, value)

	private var oldPos: Vec3 = position()

	var velocity: Vec3
		get() = scaleVecByMedia(deltaMovement)
		set(value) {
			// change the wisp to look where its velocity points, useful for blinking
			setLookVector(value)
			deltaMovement = value
		}

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
		this.caster = caster
		this.media = media
//		lastTick = world.gameTime - 1
	}

	open fun getEffectSource(): Entity {
		return MoreObjects.firstNonNull(this.caster, this)
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

		HexalAPI.LOGGER.info("wisp id is $id")

		if (!scheduledCast) {
			if (!level.isClientSide)
				deductMedia()

			oldPos = position()

			childTick()
			move()
		}

		if (level.isClientSide) {
			val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
			playWispParticles(colouriser)
			playLinkParticles(colouriser)
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
		hex: Either<List<SpellDatum<*>>, ListTag>,
		initialStack: Either<MutableList<SpellDatum<*>>, ListTag> = Either.left(ArrayList<SpellDatum<*>>().toMutableList()),
		initialRavenmind: Either<SpellDatum<*>, CompoundTag> = Either.left(SpellDatum.make(Widget.NULL)),
	): Boolean {
		if (level.isClientSide || caster == null)
			return false // return dummy data, not expecting anything to be done with it

		val sPlayer = caster as ServerPlayer

		val rHex = hex.map({ it }, { it.toIotaList(level as ServerLevel) })
		val rInitialStack = initialStack.map({ it }, { it.toIotaList(level as ServerLevel) })
		val rInitialRavenmind = initialRavenmind.map({ it }, { SpellDatum.Companion.fromNBT(it, level as ServerLevel) })

//		HexalAPI.LOGGER.info("attempting to schedule cast")

		IXplatAbstractions.INSTANCE.getWispCastingManager(sPlayer).ifPresent {
			it.scheduleCast(this, priority, rHex, rInitialStack, rInitialRavenmind)

//			HexalAPI.LOGGER.info("cast successfully scheduled, hex was $hex, stack was $initialStack, ravenmind was $initialRavenmind")

			scheduledCast = true
		}

		return scheduledCast
	}

	override fun link(other: LinkableEntity, linkOther: Boolean) {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("wisp $uuid had linkWisp called in a clientside context.")
			return
		}

		linked = Either.left(linked.map({ it }, { it.toEntityList(level as ServerLevel) }))

		if (other in linked.left().get())
			return

		HexalAPI.LOGGER.info("doing the ifLeft.")

		linked.ifLeft {
			HexalAPI.LOGGER.info("adding $other to $uuid's links.")
			it.add(other)
		}

		if (linkOther) {
			HexalAPI.LOGGER.info("adding $other to $uuid's render links.")
			addRenderLink(other)
		}

		if (linkOther) {
			link(this, false)
		}
	}

	override fun unlink(other: LinkableEntity, unlinkOther: Boolean) {
		if (level.isClientSide) {
			HexalAPI.LOGGER.info("wisp $uuid had linkWisp called in a clientside context.")
			return
		}

		linked = Either.left(linked.map({ it }, { it.toEntityList(level as ServerLevel) }))

		linked.ifLeft {
			it.remove(other)
		}
		removeRenderLink(other)

		if (unlinkOther) {
			unlink(this, false)
		}
	}

	override fun getLinked(index: Int): LinkableEntity {
		linked = Either.left(linked.map({ it }, { it.toEntityList(level as ServerLevel) }))

		return linked.left().get()[index]
	}

	override fun numLinked(): Int {
		linked = Either.left(linked.map({ it }, { it.toEntityList(level as ServerLevel) }))

		return linked.left().get().size
	}

	override fun receiveIota(iota: SpellDatum<*>) {
		receivedIotas = Either.left(receivedIotas.map({ it }, { it.toIotaList(level as ServerLevel) }))

		receivedIotas.ifLeft {
			it.add(iota)
		}
	}

	override fun nextReceivedIota(): SpellDatum<*> {
		receivedIotas = Either.left(receivedIotas.map({ it }, { it.toIotaList(level as ServerLevel) }))

		val rReceivedIotas = receivedIotas.left().get()

		if (rReceivedIotas.size == 0) {
			return SpellDatum.make(Widget.NULL)
		}

		val iota = receivedIotas.left().get()[0]
		receivedIotas.left().get().removeAt(0)

		return iota
	}

	override fun numRemainingIota(): Int {
		receivedIotas = Either.left(receivedIotas.map({ it }, { it.toIotaList(level as ServerLevel) }))

		return receivedIotas.left().get().size
	}

	open fun castCallback(result: WispCastingManager.WispCastResult) {
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

	fun playWispParticles() {
		val colouriser = FrozenColorizer.fromNBT(entityData.get(COLOURISER))
		playWispParticles(colouriser)
	}

	open protected fun playWispParticles(colouriser: FrozenColorizer) {
		val radius = ceil((media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 10)

		val delta = position() - oldPos
		val dist = delta.length() * 12 * radius * radius * radius

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
	}

	fun playLinkParticles(colouriser: FrozenColorizer) {
		HexalAPI.LOGGER.info("wisp $uuid has ${renderLinks.size} links to render")

		for (renderLink in renderLinks) {
			val delta = renderLink.position() - position()
			val dist = delta.length() * 12

			for (i in 0..dist.toInt()) {
				val colour: Int = colouriser.nextColour()

				val coeff = i / dist
				level.addParticle(
					ConjureParticleOptions(colour, false),
					(position().x + delta.x * coeff),
					(position().y + delta.y * coeff),
					(position().z + delta.z * coeff),
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
		if (compound.hasUUID(TAG_CASTER))
			casterUUID = compound.getUUID(TAG_CASTER)

		entityData.set(COLOURISER, compound.getCompound(TAG_COLOURISER))
		if (!level.isClientSide) {
			hex = Either.right(compound.get(TAG_HEX) as ListTag)
			linked = Either.right(compound.get(TAG_LINKED_WISPS) as ListTag)
			entityData.set(RENDER_LINKS, compound.get(TAG_RENDER_LINKS) as CompoundTag)
			receivedIotas = Either.right(compound.get(TAG_RECEIVED_IOTAS) as ListTag)
		}
		media = compound.getInt(TAG_MEDIA)
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		if (casterUUID != null)
			compound.putUUID(TAG_CASTER, casterUUID!!)

		compound.putCompound(TAG_COLOURISER, entityData.get(COLOURISER))
		if (!level.isClientSide) {
			compound.put(TAG_HEX, hex.map({ it.toNbtList() }, { it }))
			compound.put(TAG_LINKED_WISPS, linked.map({ it.toNbtList() }, { it }))
			compound.put(TAG_RENDER_LINKS, entityData.get(RENDER_LINKS))
			compound.put(TAG_RECEIVED_IOTAS, receivedIotas.map({ it.toNbtList() }, { it }))
		}
		compound.putInt(TAG_MEDIA, media)
	}

	override fun defineSynchedData() {
		// defines the entry in SynchedEntityData associated with the EntityDataAccessor COLOURISER, and gives it a default value
//		HexalAPI.LOGGER.info("defineSynchedData for $uuid called!")
		entityData.define(COLOURISER, FrozenColorizer.DEFAULT.get().serializeToNBT())
		entityData.define(MEDIA, 20 * ManaConstants.DUST_UNIT)
		entityData.define(SCHEDULED_CAST, false)

		val tag = CompoundTag()
		tag.put(TAG_RENDER_LINKS, ListTag())
		entityData.define(RENDER_LINKS, tag)
	}

	override fun getAddEntityPacket(): Packet<*> {
		return ClientboundAddEntityPacket(this, caster?.id ?: 0)
	}

	override fun recreateFromPacket(packet: ClientboundAddEntityPacket) {
		super.recreateFromPacket(packet)
		val caster = level.getEntity(packet.data)
		if (caster != null) {
			this.caster = caster
		}
	}

	companion object {
		@JvmField
		val COLOURISER: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)
		val MEDIA: EntityDataAccessor<Int> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.INT)
		val SCHEDULED_CAST: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.BOOLEAN)
		val RENDER_LINKS: EntityDataAccessor<CompoundTag> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.COMPOUND_TAG)

		const val TAG_CASTER = "caster"
		const val TAG_COLOURISER = "colouriser"
		const val TAG_HEX = "hex"
		const val TAG_LINKED_WISPS = "linked_wisps"
		const val TAG_RENDER_LINKS = "render_link_list"
		const val TAG_RECEIVED_IOTAS = "received_iotas"
		const val TAG_MEDIA = "media"

		const val WISP_COST_PER_TICK = (0.325 * ManaConstants.DUST_UNIT / 20.0).toInt()
	}
}

