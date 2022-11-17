package ram.talia.hexal.fabric.cc

import at.petrak.hexcasting.api.utils.hasCompound
import at.petrak.hexcasting.api.utils.putCompound
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.spell.casting.WispCastingManager
import ram.talia.hexal.common.entities.BaseCastingWisp

class CCWispCastingManager(private val player: Player, var seon: BaseCastingWisp? = null) :
		ServerTickingComponent, AutoSyncedComponent {
	val manager = (player as? ServerPlayer)?.let { WispCastingManager(it) }
	override fun serverTick() {
		manager?.executeCasts()
	}

	override fun readFromNbt(tag: CompoundTag) {
		if (player is ServerPlayer) {
			if (tag.hasCompound(TAG_MANAGER))
				manager?.readFromNbt(tag.getCompound(TAG_MANAGER), player.level as ServerLevel)
			if (tag.hasUUID(TAG_SEON))
				seon = player.getLevel().getEntity(tag.getUUID(TAG_SEON)) as? BaseCastingWisp
		}
	}

	override fun writeToNbt(tag: CompoundTag) {
		if (manager != null) {
			val manTag = CompoundTag()
			manager.writeToNbt(manTag)
			tag.putCompound(TAG_MANAGER, manTag)
		}
		if (seon != null) {
			tag.putUUID(TAG_SEON, seon!!.uuid)
		}
	}

	companion object {
		const val TAG_MANAGER = "manager"
		const val TAG_SEON = "seon"
	}
}