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
import java.util.UUID

class CCWispCastingManager(private val player: Player, var seonUUID: UUID? = null) :
		ServerTickingComponent, AutoSyncedComponent {
	val manager = (player as? ServerPlayer)?.let { WispCastingManager(it.uuid, it.server) }

	var seon: BaseCastingWisp? = null
		get() {
			if (seonUUID == null)
				return null
			if (field?.isRemoved != false)
				return (player as? ServerPlayer)?.let { it.getLevel().getEntity(seonUUID!!) as BaseCastingWisp }
			return field
		}
		set(value) {
			seonUUID = value?.uuid
			field = value
		}

	override fun serverTick() {
		manager?.executeCasts()
	}

	override fun readFromNbt(tag: CompoundTag) {
		if (player is ServerPlayer) {
			if (tag.hasCompound(TAG_MANAGER))
				manager?.readFromNbt(tag.getCompound(TAG_MANAGER), player.level as ServerLevel)
			if (tag.hasUUID(TAG_SEON))
				seonUUID = tag.getUUID(TAG_SEON)
		}
	}

	override fun writeToNbt(tag: CompoundTag) {
		if (manager != null) {
			val manTag = CompoundTag()
			manager.writeToNbt(manTag)
			tag.putCompound(TAG_MANAGER, manTag)
		}
		seonUUID?.let { tag.putUUID(TAG_SEON, it) }
	}

	companion object {
		const val TAG_MANAGER = "hexal:manager"
		const val TAG_SEON = "hexal:seon"
	}
}