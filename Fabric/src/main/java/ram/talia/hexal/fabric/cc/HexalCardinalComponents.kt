package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer
import ram.talia.hexal.api.HexalAPI

class HexalCardinalComponents : EntityComponentInitializer, ItemComponentInitializer {
	override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
		registry.registerForPlayers(LEMMA_CASTING_MANAGER, ::CCLemmaCastingManager, RespawnCopyStrategy.ALWAYS_COPY)
	}

	override fun registerItemComponentFactories(registry: ItemComponentFactoryRegistry) {
		return
	}

	companion object {
		@JvmField
		val LEMMA_CASTING_MANAGER: ComponentKey<CCLemmaCastingManager> = ComponentRegistry.getOrCreate(
			HexalAPI.modLoc("lemma_casting_manager"),
			CCLemmaCastingManager::class.java
		)
	}
}