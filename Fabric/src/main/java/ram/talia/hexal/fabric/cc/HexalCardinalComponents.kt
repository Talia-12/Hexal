package ram.talia.hexal.fabric.cc

import at.petrak.hexcasting.fabric.cc.CCFlight
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.HexalAPI

class HexalCardinalComponents : EntityComponentInitializer, ItemComponentInitializer {
	override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
		registry.registerForPlayers(WISP_CASTING_MANAGER, ::CCWispCastingManager, RespawnCopyStrategy.ALWAYS_COPY)
	}

	override fun registerItemComponentFactories(registry: ItemComponentFactoryRegistry) {
		return
	}

	companion object {
		@JvmField
		val WISP_CASTING_MANAGER: ComponentKey<CCWispCastingManager> = ComponentRegistry.getOrCreate(
			HexalAPI.modLoc("wisp_casting_manager"),
			CCWispCastingManager::class.java
		)
	}
}