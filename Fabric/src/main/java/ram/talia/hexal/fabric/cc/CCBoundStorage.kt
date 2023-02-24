package ram.talia.hexal.fabric.cc

import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import java.util.*

class CCBoundStorage(_player: Player) : Component {
    var storage: UUID? = null

    override fun readFromNbt(tag: CompoundTag) {
        if (tag.contains(TAG_BOUND_STORAGE))
            storage = tag.getUUID(TAG_BOUND_STORAGE)
    }

    override fun writeToNbt(tag: CompoundTag) {
        storage?.let { tag.putUUID(TAG_BOUND_STORAGE, it) }
    }

    companion object {
        const val TAG_BOUND_STORAGE = "hexal:bound_storage"
    }
}