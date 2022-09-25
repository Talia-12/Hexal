package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.utils.hasFloat
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nbt.LazyIota
import ram.talia.hexal.api.nbt.LazyIotaList
import ram.talia.hexal.api.plus
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.times
import ram.talia.hexal.common.lib.HexalEntities

class TickingWisp : BaseCastingWisp {
	override val shouldComplainNotEnoughMedia = false

	var stack: MutableList<SpellDatum<*>>
		get() {
			if (level.isClientSide)
				throw Exception("TickingWisp.stack should only be accessed on server.") // TODO: create and replace with ServerOnlyException
			return lazyStack!!.get()
		}
		set(value) {
			lazyStack?.set(value)
		}
	private var lazyStack: LazyIotaList? = if (level.isClientSide) null else LazyIotaList(level as ServerLevel)

	var ravenmind: SpellDatum<*>
		get() {
			if (level.isClientSide)
				throw Exception("TickingWisp.stack should only be accessed on server.") // TODO: create and replace with ServerOnlyException
			return lazyRavenmind!!.get()
		}
		set(value) {
			lazyRavenmind?.set(value)
		}
	private var lazyRavenmind: LazyIota? = if (level.isClientSide) null else LazyIota(level as ServerLevel)



	constructor(entityType: EntityType<out BaseCastingWisp>, world: Level) : super(entityType, world)
	constructor(
		entityType: EntityType<out TickingWisp>,
		world: Level,
		pos: Vec3,
		caster: Player,
		media: Int,
	) : super(entityType, world, pos, caster, media) {
		setTargetMovePos(pos)
	}

	constructor(world: Level, pos: Vec3, caster: Player, media: Int) : super(HexalEntities.TICKING_WISP, world, pos, caster, media) {
		setTargetMovePos(pos)
	}

	init {
		lazyStack?.set(mutableListOf(SpellDatum.make(this)))
		lazyRavenmind?.set(SpellDatum.make(Widget.NULL))
	}

	override fun deductMedia() {
		media -= 2 * WISP_COST_PER_TICK_NORMAL
	}

	override fun childTick() {
//		HexalAPI.LOGGER.info("ticking wisp $uuid childTick called, caster is $caster")
		if (level.isClientSide) return
		scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, stack, ravenmind)
	}

	override fun move() {
		if (reachedTargetPos())
			return

		val diffVec = getTargetMovePos() - position()
		val sqrtDist = diffVec.lengthSqr()

		if (sqrtDist < 0.01) {
			setPos(getTargetMovePos())
		}

		val distToStep = MAX_SPEED_PER_TICK * sqrtDist / (sqrtDist + SCALE) // smoothly slow down as it approaches the goal

		val step = maxMove(diffVec.normalize() * distToStep)

		setPos(position() + step)
	}

	override fun maxSqrCastingDistance() = CASTING_RADIUS * CASTING_RADIUS

	override fun castCallback(result: WispCastingManager.WispCastResult) {
//		HexalAPI.LOGGER.info("ticking wisp $uuid had a cast successfully completed!")
		stack = result.endStack
		ravenmind = result.endRavenmind

		super.castCallback(result)
	}
	fun getTargetMovePos() = Vec3(entityData.get(TARGET_MOVE_POS_X).toDouble(), entityData.get(TARGET_MOVE_POS_Y).toDouble(), entityData.get(TARGET_MOVE_POS_Z).toDouble())

	fun setTargetMovePos(pos: Vec3) {
		entityData.set(TARGET_MOVE_POS_X, pos.x.toFloat())
		entityData.set(TARGET_MOVE_POS_Y, pos.y.toFloat())
		entityData.set(TARGET_MOVE_POS_Z, pos.z.toFloat())
	}

	fun reachedTargetPos() = (getTargetMovePos() - position()).lengthSqr() < 0.0001*0.0001

					override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		when (val stackTag = compound.get(TAG_STACK)) {
			null -> lazyStack!!.set(mutableListOf())
			else -> lazyStack!!.set(stackTag as ListTag)
		}
		when (val ravenmindTag = compound.getCompound(TAG_RAVENMIND)) {
			null -> lazyRavenmind!!.set(SpellDatum.make(Widget.NULL))
			else -> lazyRavenmind!!.set(ravenmindTag)
		}

		entityData.set(TARGET_MOVE_POS_X, when(compound.hasFloat(TAG_TARGET_MOVE_POS_X)) {
			true -> compound.getFloat(TAG_TARGET_MOVE_POS_X)
			false -> position().x.toFloat()
		})
		entityData.set(TARGET_MOVE_POS_Y, when(compound.hasFloat(TAG_TARGET_MOVE_POS_Y)) {
			true -> compound.getFloat(TAG_TARGET_MOVE_POS_Y)
			false -> position().y.toFloat()
		})
		entityData.set(TARGET_MOVE_POS_Z, when(compound.hasFloat(TAG_TARGET_MOVE_POS_Z)) {
			true -> compound.getFloat(TAG_TARGET_MOVE_POS_Z)
			false -> position().z.toFloat()
		})
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		compound.put(TAG_STACK, lazyStack!!.getUnloaded())
		compound.put(TAG_RAVENMIND, lazyRavenmind!!.getUnloaded())
		compound.putFloat(TAG_TARGET_MOVE_POS_X, entityData.get(TARGET_MOVE_POS_X))
		compound.putFloat(TAG_TARGET_MOVE_POS_Y, entityData.get(TARGET_MOVE_POS_Y))
		compound.putFloat(TAG_TARGET_MOVE_POS_Z, entityData.get(TARGET_MOVE_POS_Z))
	}

	override fun defineSynchedData() {
		super.defineSynchedData()

		entityData.define(TARGET_MOVE_POS_X, position().x.toFloat())
		entityData.define(TARGET_MOVE_POS_Y, position().y.toFloat())
		entityData.define(TARGET_MOVE_POS_Z, position().z.toFloat())
	}

	companion object {
		val TARGET_MOVE_POS_Y: EntityDataAccessor<Float> = SynchedEntityData.defineId(TickingWisp::class.java, EntityDataSerializers.FLOAT)
		val TARGET_MOVE_POS_Z: EntityDataAccessor<Float> = SynchedEntityData.defineId(TickingWisp::class.java, EntityDataSerializers.FLOAT)
		val TARGET_MOVE_POS_X: EntityDataAccessor<Float> = SynchedEntityData.defineId(TickingWisp::class.java, EntityDataSerializers.FLOAT)

		const val TAG_STACK = "stack"
		const val TAG_RAVENMIND = "ravenmind"
		const val TAG_TARGET_MOVE_POS_X = "target_move_pos_x"
		const val TAG_TARGET_MOVE_POS_Y = "target_move_pos_y"
		const val TAG_TARGET_MOVE_POS_Z = "target_move_pos_z"

		const val CASTING_SCHEDULE_PRIORITY = -5
		const val CASTING_RADIUS = 8.0

		const val MAX_SPEED_PER_TICK = 2.0 / 20
		const val SCALE = 0.2
	}
}