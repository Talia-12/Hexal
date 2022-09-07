package ram.talia.hexal.common.blocks.entity

import at.petrak.hexcasting.api.block.HexBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import ram.talia.hexal.common.lib.HexalBlockEntities

class BlockEntitySlipway(val pos: BlockPos, val state: BlockState) : HexBlockEntity(HexalBlockEntities.SLIPWAY, pos, state) {

	private var isActive = false

	fun tick() {
		if (level!!.isClientSide)
			clientTick()
		else
			serverTick()
	}

	private fun clientTick() {

	}

	private fun serverTick() {

	}

	override fun saveModData(tag: CompoundTag) {
		tag.putBoolean(TAG_IS_ACTIVE, isActive)
	}

	override fun loadModData(tag: CompoundTag) {
		isActive = tag.getBoolean(TAG_IS_ACTIVE)
	}

	companion object {
		const val TAG_IS_ACTIVE = "is_active"
	}
}