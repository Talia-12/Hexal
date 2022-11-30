package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.nextColour
import ram.talia.hexal.api.nextGaussian
import ram.talia.hexal.common.entities.WanderingWisp
import ram.talia.hexal.common.lib.HexalBlockEntities
import java.util.*

class BlockEntitySlipway(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.SLIPWAY, pos, state) {

	private val random = RandomSource.create()

	private var isActive = false

	private var nextSpawnTick: Long = 0

	fun tick() {
		if (level == null)
			return
		if (level!!.isClientSide)
			clientTick()
		else
			serverTick()
	}

	private fun clientTick() {
		val vec = Vec3.atCenterOf(pos)

		for (colouriser in HexItems.DYE_COLORIZERS.values) {
			val frozenColouriser = FrozenColorizer(ItemStack(colouriser), Util.NIL_UUID)
			val colour: Int = frozenColouriser.nextColour(random)

			level!!.addParticle(
					ConjureParticleOptions(colour, true),
					(vec.x + RENDER_RADIUS * random.nextGaussian()),
					(vec.y + RENDER_RADIUS * random.nextGaussian()),
					(vec.z + RENDER_RADIUS * random.nextGaussian()),
					0.0125 * (random.nextDouble() - 0.5),
					0.0125 * (random.nextDouble() - 0.5),
					0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	private fun serverTick() {
		if (!isActive) {
			if (level!!.hasNearbyAlivePlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), ACTIVE_RANGE)) {
				isActive = true
				sync()
			}

			return
		}

		val tick = level!!.gameTime

		if (tick >= nextSpawnTick) {
			nextSpawnTick = tick + random.nextGaussian(SPAWN_INTERVAL_MU.toDouble(), SPAWN_INTERVAL_SIG.toDouble()).toLong()

			val colouriser = getRandomColouriser()

			val wisp = WanderingWisp(level!!, Vec3.atCenterOf(pos))
			wisp.setColouriser(colouriser)
			level!!.addFreshEntity(wisp)

			sync()
		}
	}

	override fun saveModData(tag: CompoundTag) {
		tag.putBoolean(TAG_IS_ACTIVE, isActive)
		tag.putLong(TAG_NEXT_SPAWN_TICK, nextSpawnTick)
	}

	override fun loadModData(tag: CompoundTag) {
		isActive = tag.getBoolean(TAG_IS_ACTIVE)
		nextSpawnTick = tag.getLong(TAG_NEXT_SPAWN_TICK)
	}

	companion object {
		const val TAG_IS_ACTIVE = "is_active"
		const val TAG_NEXT_SPAWN_TICK = "last_spawned_tick"

		const val ACTIVE_RANGE = 5.0
		const val SPAWN_INTERVAL_MU = 80
		const val SPAWN_INTERVAL_SIG = 10

		const val RENDER_RADIUS = 0.5

		private val RANDOM = Random()

		fun getRandomColouriser(): FrozenColorizer {
			return FrozenColorizer(ItemStack(HexItems.DYE_COLORIZERS.values.elementAt(RANDOM.nextInt(HexItems.DYE_COLORIZERS.size))), Util.NIL_UUID)
		}
	}
}