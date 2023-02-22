package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer
import ram.talia.hexal.api.HexalAPI.modLoc

class HexalCardinalComponents : EntityComponentInitializer, ItemComponentInitializer {
	override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
		registry.registerForPlayers(BOUND_STORAGE, ::CCBoundStorage, RespawnCopyStrategy.ALWAYS_COPY)
		registry.registerForPlayers(EVERBOOK, ::CCEverbook, RespawnCopyStrategy.ALWAYS_COPY)
		registry.registerForPlayers(PLAYER_LINKSTORE, ::CCPlayerLinkstore, RespawnCopyStrategy.NEVER_COPY)
		registry.registerForPlayers(WISP_CASTING_MANAGER, ::CCWispCastingManager, RespawnCopyStrategy.ALWAYS_COPY)
	}

	override fun registerItemComponentFactories(registry: ItemComponentFactoryRegistry) {
		return
	}

	companion object {
		@JvmField
		val BOUND_STORAGE: ComponentKey<CCBoundStorage> = ComponentRegistry.getOrCreate(
				modLoc("bound_storage"),
				CCBoundStorage::class.java
		)
		@JvmField
		val EVERBOOK: ComponentKey<CCEverbook> = ComponentRegistry.getOrCreate(
				modLoc("everbook"),
				CCEverbook::class.java
		)
		@JvmField
		val PLAYER_LINKSTORE: ComponentKey<CCPlayerLinkstore> = ComponentRegistry.getOrCreate(
				modLoc("player_linkstore"),
				CCPlayerLinkstore::class.java
		)
		@JvmField
		val WISP_CASTING_MANAGER: ComponentKey<CCWispCastingManager> = ComponentRegistry.getOrCreate(
			modLoc("wisp_casting_manager"),
			CCWispCastingManager::class.java
		)
	}
}