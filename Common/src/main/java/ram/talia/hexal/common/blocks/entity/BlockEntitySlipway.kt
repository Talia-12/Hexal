package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.addldata.Colorizer
import at.petrak.hexcasting.api.block.HexBlockEntity
import at.petrak.hexcasting.api.item.ColorizerItem
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.common.entities.ProjectileWisp
import ram.talia.hexal.common.entities.TickingWisp
import ram.talia.hexal.common.entities.WanderingWisp
import ram.talia.hexal.common.lib.HexalBlockEntities
import java.util.*

class BlockEntitySlipway(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.SLIPWAY, pos, state) {

	private var isActive = false

	private var nextSpawnTick: Long = 0

	fun tick() {
		if (level!!.isClientSide)
			clientTick()
		else
			serverTick()
	}

	private fun clientTick() {

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
			nextSpawnTick = tick + SPAWN_INTERVAL

			HexalAPI.LOGGER.info("spawned at pos $pos, tick $tick")
			val colouriser = getRandomColouriser()

			val wisp = WanderingWisp(level!!, Vec3.atCenterOf(pos), 30 * ManaConstants.DUST_UNIT)
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
		const val SPAWN_INTERVAL = 80 //TODO: make this a bit random.

		val RANDOM = Random()

		fun getRandomColouriser(): FrozenColorizer {
			return FrozenColorizer(ItemStack(HexItems.DYE_COLORIZERS.values.elementAt(RANDOM.nextInt(HexItems.DYE_COLORIZERS.size))), Util.NIL_UUID)
		}
	}
}