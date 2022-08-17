package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ram.talia.hexal.api.spell.casting.LemmaCastingManager

public class CCLemmaCastingManager(private val owner: Player) : ServerTickingComponent, AutoSyncedComponent {
	val manager = (owner as? ServerPlayer)?.let { LemmaCastingManager(it) }

	override fun serverTick() {
		manager?.executeCasts()
	}

	override fun readFromNbt(tag: CompoundTag) {
		if (owner is ServerPlayer)
			manager?.readFromNbt(tag, owner.level as ServerLevel)
	}

	override fun writeToNbt(tag: CompoundTag) {
		manager?.writeToNbt(tag)
	}
}