package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import com.mojang.datafixers.util.Either
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
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

class TickingWisp : BaseWisp {
	override val shouldComplainNotEnoughMedia = false



	var stack: MutableList<SpellDatum<*>>
		get() {
			resolveStack()
			return stackEither.left().get()
		}
		set(value) {
			stackEither = Either.left(value)
		}
	private var stackEither: Either<MutableList<SpellDatum<*>>, ListTag> = Either.left(mutableListOf(SpellDatum.make(this)))

	var ravenmind: SpellDatum<*>
		get() {
			resolveRavenmind()
			return ravenmindEither.left().get()
		}
		set(value) {
			ravenmindEither = Either.left(value)
		}
	private var ravenmindEither: Either<SpellDatum<*>, CompoundTag> = Either.left(SpellDatum.make(Widget.NULL))

	private fun resolveStack() {
		stackEither.ifRight { listTag -> stackEither = Either.left(listTag.toIotaList(level as ServerLevel)) }
	}

	private fun resolveRavenmind() {
		ravenmindEither.ifRight { iotaTag -> ravenmindEither = Either.left(SpellDatum.Companion.fromNBT(iotaTag, level as ServerLevel)) }
	}

	constructor(entityType: EntityType<out BaseWisp>, world: Level) : super(entityType, world)
	constructor(
		entityType: EntityType<out TickingWisp>,
		world: Level,
		pos: Vec3,
		caster: Player,
		media: Int,
	) : super(entityType, world, pos, caster, media)

	constructor(world: Level, pos: Vec3, caster: Player, media: Int) : super(HexalEntities.TICKING_WISP, world, pos, caster, media)

	override fun deductMedia() {
		media -= 2 * WISP_COST_PER_TICK
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

		if (result.makesCastSound) {
			level.playSound(
				null, position().x, position().y, position().z,
				HexSounds.ACTUALLY_CAST, SoundSource.PLAYERS, 0.05f,
				1f + (random.nextFloat() - 0.5f) * 0.2f
			)
		}

		super.castCallback(result)
	}

	override fun playWispParticles(colouriser: FrozenColorizer) {
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

	override fun readAdditionalSaveData(compound: CompoundTag) {
		super.readAdditionalSaveData(compound)

		stackEither = when (val stackTag = compound.get(STACK_TAG)) {
			null -> Either.left(mutableListOf())
			else -> Either.right(stackTag as ListTag)
		}
		ravenmindEither = when (val ravenmindTag = compound.getCompound(RAVENMIND_TAG)) {
			null -> Either.left(SpellDatum.make(Widget.NULL))
			else -> Either.right(ravenmindTag)
		}
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		super.addAdditionalSaveData(compound)

		stackEither.map(
			{compound.put(STACK_TAG, it.toNbtList())},
			{compound.put(STACK_TAG, it)}
		)
//		HexalAPI.LOGGER.info("saved wisp $uuid's stack as ${compound.get(STACK_TAG)}, was $stackEither")
		ravenmindEither.map(
			{compound.put(RAVENMIND_TAG, it.serializeToNBT())},
			{compound.put(RAVENMIND_TAG, it)}
		)
//		HexalAPI.LOGGER.info("saved wisp $uuid's ravenmind as ${compound.get(RAVENMIND_TAG)}, was $ravenmindEither")
	}

	companion object {
		const val STACK_TAG = "stack"
		const val RAVENMIND_TAG = "ravenmind"

		const val CASTING_SCHEDULE_PRIORITY = -5
		const val CASTING_RADIUS = 8.0
	}
}