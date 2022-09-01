package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.api.spell.toIotaList
import ram.talia.hexal.api.spell.toNbtList
import kotlin.math.pow
import kotlin.math.sqrt

class TickingWisp : BaseWisp {
	override val shouldComplainNotEnoughMedia = false

	var lasting: Boolean
		get() = entityData.get(LASTING)
		set(value) = entityData.set(LASTING, value)

	private var stack: Either<MutableList<SpellDatum<*>>, ListTag> = Either.left(mutableListOf(SpellDatum.make(this)))
	private var ravenmind: Either<SpellDatum<*>, CompoundTag> = Either.left(SpellDatum.make(Widget.NULL))

	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)
	constructor(
		entityType: EntityType<out TickingWisp>,
		world: Level,
		pos: Vec3,
		caster: Player,
		media: Int,
		lasting: Boolean
	) : super(entityType, world, pos, caster, media) {
		this.lasting = lasting
	}

	constructor(world: Level, pos: Vec3, caster: Player, media: Int, lasting: Boolean) : super(HexalEntities.TICKING_WISP, world, pos, caster, media) {
		this.lasting = lasting
	}

	override fun deductMedia() {
		val deduct = when (lasting) {
			true -> WISP_COST_PER_TICK
			false -> WISP_COST_PER_TICK + (TICK_COST_EXP_SCALE * sqrt(media.toDouble())).toInt()
		}

//		HexalAPI.LOGGER.info("ticking wisp $uuid had ${deduct.toDouble()/ManaConstants.DUST_UNIT} media deducted.")

//		HexalAPI.LOGGER.info("media before: ${media.toDouble()/ManaConstants.DUST_UNIT}")
		media -= deduct
//		HexalAPI.LOGGER.info("media after: ${media.toDouble()/ManaConstants.DUST_UNIT}")
	}

	override fun childTick() {
		scheduleCast(CASTING_SCHEDULE_PRIORITY, hex, stack, ravenmind)
	}

	override fun move() {}

	override fun maxSqrCastingDistance() = if (lasting) CASTING_RADIUS_LASTING * CASTING_RADIUS_LASTING else CASTING_RADIUS_NONLASTING * CASTING_RADIUS_NONLASTING

	override fun castCallback(result: WispCastingManager.WispCastResult) {
//		HexalAPI.LOGGER.info("ticking wisp $uuid had a cast successfully completed!")
		stack = Either.left(result.endStack)
		ravenmind = Either.left(result.endRavenmind)

		if (result.makesCastSound) {
			level.playSound(
				null, position().x, position().y, position().z,
				HexSounds.ACTUALLY_CAST, SoundSource.PLAYERS, 0.05f,
				1f + (random.nextFloat() - 0.5f) * 0.2f
			)
		}

		super.castCallback(result)
	}

	override fun playParticles(colouriser: FrozenColorizer) {
		val radius = (media.toDouble() / ManaConstants.DUST_UNIT).pow(1.0 / 3) / 100

		for (i in 0..50) {
			val colour: Int = colouriser.nextColour()

			level.addParticle(
				ConjureParticleOptions(colour, true),
				(position().x + radius*random.nextGaussian()),
				(position().y + radius*random.nextGaussian()),
				(position().z + radius*random.nextGaussian()),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	override fun load(compound: CompoundTag) {
		super.load(compound)
		lasting = compound.getBoolean(LASTING_TAG)
		if (level is ServerLevel) {
			val stackNbt = compound.get(STACK_TAG) as ListTag
			HexalAPI.LOGGER.info("loading wisp $uuid's stack from $stackNbt")
			stack = Either.right(stackNbt)
			HexalAPI.LOGGER.info("loaded wisp $uuid's stack as $stack")
			ravenmind = Either.right(compound.getCompound(RAVENMIND_TAG))
		}
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)
		compound.putBoolean(LASTING_TAG, lasting)
		if (level is ServerLevel) {
			stack.map(
				{compound.put(STACK_TAG, it.toNbtList())},
				{compound.put(STACK_TAG, it)}
			)
			HexalAPI.LOGGER.info("saved wisp $uuid's stack as ${compound.get(STACK_TAG)}")
			ravenmind.map(
				{compound.put(RAVENMIND_TAG, it.serializeToNBT())},
				{compound.put(RAVENMIND_TAG, it)}
			)
			HexalAPI.LOGGER.info("saved wisp $uuid's ravenmind as ${compound.get(RAVENMIND_TAG)}")
		}
	}

	override fun defineSynchedData() {
		super.defineSynchedData()
		entityData.define(LASTING, false)
	}

	companion object {
		val LASTING: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(TickingWisp::class.java, EntityDataSerializers.BOOLEAN)

		const val LASTING_TAG = "lasting"
		const val STACK_TAG = "stack"
		const val RAVENMIND_TAG = "ravenmind"

		const val CASTING_SCHEDULE_PRIORITY = -5
		const val CASTING_RADIUS_LASTING = 16.0
		const val CASTING_RADIUS_NONLASTING = 8.0

		const val TICK_COST_EXP_SCALE = 1.0/60
	}
}