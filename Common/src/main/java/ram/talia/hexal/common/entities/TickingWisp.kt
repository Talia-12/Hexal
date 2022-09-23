package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.nbt.LazyIota
import ram.talia.hexal.api.nbt.LazyIotaList
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.spell.toNbtList
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
	) : super(entityType, world, pos, caster, media)

	constructor(world: Level, pos: Vec3, caster: Player, media: Int) : super(HexalEntities.TICKING_WISP, world, pos, caster, media)

	init {
		lazyStack?.set(mutableListOf(SpellDatum.make(this)))
		lazyRavenmind?.set(SpellDatum.make(Widget.NULL))
	}

	override fun deductMedia() {
		media -= 2 * WISP_COST_PER_TICK_NORMAL
	}

	override fun childTick() {
//		HexalAPI.LOGGER.info("ticking wisp $uuid childTick called, caster is $caster")
		scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, stack, ravenmind)
	}

	override fun move() {}

	override fun maxSqrCastingDistance() = CASTING_RADIUS * CASTING_RADIUS

	override fun castCallback(result: WispCastingManager.WispCastResult) {
//		HexalAPI.LOGGER.info("ticking wisp $uuid had a cast successfully completed!")
		stack = result.endStack
		ravenmind = result.endRavenmind

		super.castCallback(result)
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		when (val stackTag = compound.get(STACK_TAG)) {
			null -> lazyStack!!.set(mutableListOf())
			else -> lazyStack!!.set(stackTag as ListTag)
		}
		when (val ravenmindTag = compound.getCompound(RAVENMIND_TAG)) {
			null -> lazyRavenmind!!.set(SpellDatum.make(Widget.NULL))
			else -> lazyRavenmind!!.set(ravenmindTag)
		}
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		compound.put(STACK_TAG, lazyStack!!.getUnloaded())
//		HexalAPI.LOGGER.info("saved wisp $uuid's stack as ${compound.get(STACK_TAG)}, was $stackEither")
		compound.put(RAVENMIND_TAG, lazyRavenmind!!.getUnloaded())
//		HexalAPI.LOGGER.info("saved wisp $uuid's ravenmind as ${compound.get(RAVENMIND_TAG)}, was $ravenmindEither")
	}

	companion object {
		const val STACK_TAG = "stack"
		const val RAVENMIND_TAG = "ravenmind"

		const val CASTING_SCHEDULE_PRIORITY = -5
		const val CASTING_RADIUS = 8.0
	}
}